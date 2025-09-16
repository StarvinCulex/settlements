package com.outlook.hxsw.settlements.engine.schedule;

import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;

public class ScheduleController implements Scheduler {
    static final int TIMER_WHEEL_SIZE = 3600;

    public ScheduleController(SettlementsData data, int phase) {
        this.data = data;
        this.currentPhase = phase;
    }

    public DataAccessor getData() {
        return new DataAccessor();
    }

    public void consumeServerTasks(MinecraftServer server) {
        ServerAdapter adapter = new ServerAdapter(server);
        adapter.consumeFromServerTaskQueue();
        adapter.consumeFromServerTaskBuffer();
    }

    public void startSidecar() {
        sidecar.start();
    }

    public void stopSidecar() {
        sidecar.stop();
    }

    public void sidecarTick() {
        run(sidecar::tick);
    }

    @Override
    public void run(Consumer<Scheduler.Sidecar> sidecarTask) {
        sidecarTaskQueue.add(sidecarTask);
    }

    @Override
    public void run(ServerTask serverTask) {
        serverTaskQueue.add(serverTask);
    }

    @Override
    public void register(ServerTask serverTask) {
        serverTaskBuffer.add(serverTask);
    }

    @Override
    public void unregister(ServerTask serverTask) {
        serverTaskBuffer.remove(serverTask);
    }

    @Override
    public int getPhase() {
        return currentPhase;
    }

    protected Scheduler.Sidecar getSidecar() {
        return Objects.requireNonNull(sidecar);
    }

    private final Sidecar sidecar = new Sidecar();
    private final SettlementsData data;
    private final ReentrantLock dataLock = new ReentrantLock(true);
    private volatile boolean needPause = false;
    private volatile boolean needStop = false;
    private volatile int currentPhase;

    private final BlockingQueue<Consumer<Scheduler.Sidecar>> sidecarTaskQueue = new LinkedBlockingQueue<>();
    private final TransferQueue<ServerTask> serverTaskQueue = new LinkedTransferQueue<>();
    private final Set<ServerTask> serverTaskBuffer = ConcurrentHashMap.newKeySet();

    public final class DataAccessor implements Supplier<SettlementsData>, AutoCloseable {
        private DataAccessor() {
            needPause = true;
            dataLock.lock();
            needPause = false;
        }

        @Override
        public SettlementsData get() {
            return data;
        }

        @Override
        public void close() {
            dataLock.unlock();
        }
    }

    private class ServerAdapter implements Scheduler.Server {
        private final MinecraftServer server;

        ServerAdapter(MinecraftServer server) {
            this.server = server;
        }

        void consumeFromServerTaskQueue() {
            List<ServerTask> tasks = new ArrayList<>();
            serverTaskQueue.drainTo(tasks);
            tasks.forEach(this::consume);
        }

        void consumeFromServerTaskBuffer() {
            serverTaskBuffer.removeIf(this::consume);
        }

        boolean consume(ServerTask t) {
            if (t.trigger().test(server())) {
                t.task().accept(this);
                return true;
            }
            return false;
        }

        @Override
        public MinecraftServer server() {
            return server;
        }
        @Override
        public void run(ServerTask t) {
            consume(t);
        }
        @Override
        public void register(ServerTask serverTask) {
            ScheduleController.this.register(serverTask);
        }
        @Override
        public void unregister(ServerTask serverTask) {
            ScheduleController.this.unregister(serverTask);
        }
        @Override
        public int getPhase() {
            return ScheduleController.this.getPhase();
        }
        @Override
        public void run(Consumer<Scheduler.Sidecar> sidecarTask) {
            ScheduleController.this.run(sidecarTask);
        }
    }

    private class Sidecar implements Scheduler.Sidecar {
        private final Queue<Consumer<Sidecar>> localQueue = new ArrayDeque<>();
        @SuppressWarnings("unchecked")
        private final List<Optional<SidecarTimer>>[] timerWheel = new ArrayList[TIMER_WHEEL_SIZE];
        private int localPhase;
        private Runner runner = null;

        Sidecar() {
            this.localPhase = currentPhase;
        }

        void start() {
            if (runner == null) {
                runner = new Runner();
                runner.start();
            }
        }

        void stop() {
            needStop = true;
            dataLock.lock();
            needStop = false;
            dataLock.unlock();
        }

        private class Runner extends Thread {
            @Override
            public void run() {
                try {
                    while (!needStop) {
                        var task = sidecarTaskQueue.take();
                        dataLock.lock();
                        try {
                            task.accept(ScheduleController.Sidecar.this);
                            consumeFromLocalQueue();
                        } finally {
                            dataLock.unlock();
                        }
                    }
                } catch (InterruptedException ignored) {}
            }
        }

        private void tick(Scheduler.Sidecar s) {
            try {
                executeTimer();
                consumeFromLocalQueue();
            } catch (InterruptedException ignored) {}
            morphPhase();
        }

        private void consumeFromLocalQueue() throws InterruptedException {
            while (!localQueue.isEmpty()) {
                localQueue.poll().accept(this);
                nap();
            }
        }

        private void executeTimer() throws InterruptedException {
            var tasks = timerWheel[localPhase];
            if (tasks == null) {
                return;
            }
            for (int i = 0; i < tasks.size(); i++) { // 因为accept方法可能在列表尾插入元素，所以需要用索引遍历。
                tasks.get(i).ifPresent(t -> t.task().accept(this));
            }
            tasks.removeIf(Optional::isEmpty);
        }

        private void nap() throws InterruptedException {
            if (needPause) {
                dataLock.unlock();
                dataLock.lockInterruptibly();
            }
        }

        private void morphPhase() {
            if (++localPhase == TIMER_WHEEL_SIZE) {
                localPhase = 0;
            }
            currentPhase = localPhase;
        }

        @Override
        public SettlementsData data() {
            return data;
        }

        @Override
        public int getPhase() {
            return localPhase;
        }

        @Override
        public void register(SidecarTimer timer) {
            for (int i = timer.phase(); i < timerWheel.length; i += timer.period()) {
                if (timerWheel[i] == null) {
                    timerWheel[i] = new ArrayList<>(2);
                }
                timerWheel[i].add(Optional.of(timer));
            }
        }

        @Override
        public void unregister(SidecarTimer timer) {
            for (int i = timer.phase(); i < timerWheel.length; i += timer.period()) {
                var tasks = timerWheel[i];
                if (tasks == null) {
                    continue;
                }
                tasks.replaceAll(x -> x.map(t -> t.equals(timer) ? null : t));
            }
        }

        @Override
        public void run(Consumer<Sidecar> sidecarTask) {
            localQueue.add(sidecarTask);
        }

        @Override
        public void run(ServerTask serverTask) {
            ScheduleController.this.run(serverTask);
        }
        @Override
        public void register(ServerTask serverTask) {
            ScheduleController.this.register(serverTask);
        }
        @Override
        public void unregister(ServerTask serverTask) {
            ScheduleController.this.unregister(serverTask);
        }
    }
}
