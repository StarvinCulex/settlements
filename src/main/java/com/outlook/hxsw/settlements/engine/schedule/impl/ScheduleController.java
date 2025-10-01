package com.outlook.hxsw.settlements.engine.schedule.impl;

import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.schedule.*;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;

public class ScheduleController {
    static final int TIMER_WHEEL_SIZE = 3600;

    public ScheduleController(SettlementsData data, int phase) {
        this.sidecar = new Data(data, phase);
    }

    public DataAccessor getData() {
        return new DataAccessor();
    }

    public void consumeServerTasks(MinecraftServer server) {
        ServerAdapter adapter = new ServerAdapter(server);
        adapter.consumeFromQueue();
        adapter.consumeFromBuffer();
    }

    public void startSidecar() {
        sidecar.start();
    }

    public void stopSidecar() {
        sidecar.stop();
    }

    public void sidecarTick() {
        dataTaskQueue.add(Data::tick);
    }

    protected DataScheduler getSidecar() {
        return Objects.requireNonNull(sidecar);
    }

    public final <R, T extends Task<DataScheduler, R>>
    ScheduledTask<R, T> dataSchedule(T task) {
        var t = new SchedulingOnceTask<R, Data, T>(task);
        dataTaskQueue.add(t);
        return t;
    }


    public final <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> serverSchedule(T task) {
        var t = new SchedulingOnceTask<R, ServerAdapter, T>(task);
        serverTaskQueue.add(t);
        return t;
    }

    public final <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> serverScheduleIf(Condition condition, T task) {
        var t = new SchedulingConditionalOnceTask<R, ServerAdapter, T, Condition>(condition, task);
        serverTaskQueue.add(t);
        return t;
    }

    public final <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> serverScheduleWhen(Condition condition, T task) {
        var scheduling = new SchedulingConditionalBufferedTask<R, ServerAdapter, T, Condition>(condition, task);
        var triggers = condition.triggers();
        if (triggers.isEmpty()) {
            serverTriggerlessTaskBuffer.add(scheduling);
        }
        for (Trigger trigger : triggers) {
            var queue = serverTaskBuffer.computeIfAbsent(trigger, x -> new ConcurrentLinkedQueue<>());
            queue.add(scheduling);
        }
        return scheduling;
    }

    private final Data sidecar;

    private final BlockingQueue<OnceTask<Data>> dataTaskQueue = new LinkedBlockingQueue<>();
    private final Queue<OnceTask<ServerAdapter>>
            serverTaskQueue = new ConcurrentLinkedQueue<>();
    private final Map<Trigger, Queue<SchedulingTask<?, ServerAdapter, ?>>>
            serverTaskBuffer = new ConcurrentHashMap<>();
    private final Queue<SchedulingTask<?, ServerAdapter, ?>>
            serverTriggerlessTaskBuffer = new ConcurrentLinkedQueue<>();

    public final class DataAccessor implements Supplier<SettlementsData>, AutoCloseable {
        private DataAccessor() {
            sidecar.needPause = true;
            sidecar.dataLock.lock();
            sidecar.needPause = false;
        }

        public int phase() {
            return sidecar.getPhase();
        }

        @Override
        public SettlementsData get() {
            return sidecar.data;
        }

        @Override
        public void close() {
            sidecar.dataLock.unlock();
        }
    }

    final class ServerAdapter implements ServerScheduler {
        private final MinecraftServer server;
        private final Set<Trigger> triggers;

        ServerAdapter(MinecraftServer server) {
            this.server = server;
            this.triggers = Trigger.generateTriggers(server);
        }

        void consumeFromQueue() {
            for (var t = serverTaskQueue.poll(); t != null; t = serverTaskQueue.poll()) {
                t.tryExecute(this);
            }
        }

        void consumeFromBuffer() {
            consumeBuffer(serverTriggerlessTaskBuffer);
            triggers.forEach(t -> consumeBuffer(serverTaskBuffer.get(t)));
        }

        private void consumeBuffer(Queue<SchedulingTask<?, ServerAdapter, ?>> buffer) {
            if (buffer == null) {
                return;
            }
            buffer.removeIf(t -> {
                t.tryExecute(this);
                return t.needsRecycling();
            });
        }

        @Override
        public MinecraftServer server() {
            return server;
        }

        @Override
        public Set<Trigger> triggers() {
            return triggers;
        }

        @Override
        public <R, T extends Task<DataScheduler, R>> ScheduledTask<R, T> schedule(T task) {
            return dataSchedule(task);
        }

        @Override
        public <R, T extends Task<ServerScheduler, R>> ScheduledTask<R, T> run(T task) {
            return serverSchedule(task);
        }

        @Override
        public <R, T extends Task<ServerScheduler, R>> ScheduledTask<R, T> runIf(Condition condition, T task) {
            return serverScheduleIf(condition, task);
        }

        @Override
        public <R, T extends Task<ServerScheduler, R>> ScheduledTask<R, T> runWhen(Condition condition, T task) {
            return serverScheduleIf(condition, task);
        }
    }

    final class Data implements DataScheduler {
        private final SettlementsData data;
        private int phase;

        private final ReentrantLock dataLock = new ReentrantLock(true);
        private volatile boolean needPause = false;
        private volatile boolean needStop = false;

        private final List<SchedulingTask<?, Data, ?>> localBuffer = new ArrayList<>();
        @SuppressWarnings("unchecked")
        private final List<SchedulingTask<?, Data, ?>>[] timerWheel = new ArrayList[TIMER_WHEEL_SIZE];
        private Runner runner = null;

        Data(SettlementsData data, int phase) {
            this.data = data;
            this.phase = phase;
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
            public Runner() {
                super("SettlementsSidecarRunner");
            }

            @Override
            public void run() {
                try {
                    while (!needStop) {
                        var task = dataTaskQueue.take();
                        dataLock.lock();
                        try {
                            task.tryExecute(ScheduleController.Data.this);
                            consumeFromLocalQueue();
                        } finally {
                            dataLock.unlock();
                        }
                    }
                } catch (InterruptedException ignored) {}
            }
        }

        private void tick() {
            try {
                // 只有这个顺序才能保证runPeriodically不会在当前phase就执行。
                executeFromWheel();
                consumeFromLocalQueue();
            } catch (InterruptedException ignored) {}
            morphPhase();
        }

        private void consumeFromLocalQueue() throws InterruptedException {
            for (int i = 0; i < localBuffer.size(); i++) {
                localBuffer.get(i).tryExecute(this);
                nap();
            }
            localBuffer.removeIf(SchedulingTask::needsRecycling);
        }

        private void executeFromWheel() throws InterruptedException {
            var tasks = timerWheel[phase];
            if (tasks == null) {
                return;
            }
            // 因为可能在列表尾插入元素，所以需要用索引遍历。我们不希望遍历新插入的元素。
            int size = tasks.size();
            for (int i = 0; i < size; i++) {
                tasks.get(i).tryExecute(this);
            }
            tasks.removeIf(SchedulingTask::needsRecycling);
        }

        private void nap() throws InterruptedException {
            if (needPause) {
                dataLock.unlock();
                dataLock.lockInterruptibly();
            }
        }

        private void morphPhase() {
            if (++phase == TIMER_WHEEL_SIZE) {
                phase = 0;
            }
        }

        @Override
        public SettlementsData data() {
            return data;
        }

        @Override
        public int getPhase() {
            return phase;
        }

        @Override
        public <R, T extends Task<DataScheduler, R>> ScheduledTask<R, T> run(T task) {
            var scheduling = new SchedulingOnceTask<R, Data, T>(task);
            localBuffer.add(scheduling);
            return scheduling;
        }

        @Override
        public <R, T extends Task<DataScheduler, R>> ScheduledTask<R, T> runPeriodically(int phase, int period, T task) {
            var timer = new SchedulingRepeatingTask<R, Data, T>(task);
            phase = (phase % period + period) % period;
            for (int i = phase; i < timerWheel.length; i += period) {
                registerToTimerWheel(i, timer);
            }
            return timer;
        }

        @Override
        public <R, T extends Task<DataScheduler, R>> ScheduledTask<R, T> runWhen(Predicate<DataScheduler> condition, T task) {
            var scheduling = new SchedulingConditionalBufferedTask<R, Data, T, Predicate<DataScheduler>>(condition, task);
            localBuffer.add(scheduling);
            return scheduling;
        }

        @Override
        public <R, T extends Task<DataScheduler, R>> ScheduledTask<R, T> runAfter(int delayPhases, T task) {
            if (delayPhases >= TIMER_WHEEL_SIZE || delayPhases <= 0) {
                throw new IllegalArgumentException("delayPhases >= WHEEL_SIZE or delayPhases <= 0");
            }
            var delayedTask = new SchedulingOnceTask<R, Data, T>(task);
            registerToTimerWheel(phase + delayPhases, delayedTask);
            return delayedTask;
        }

        @Override
        public <R, T extends Task<ServerScheduler, R>> ScheduledTask<R, T> schedule(T task) {
            return serverSchedule(task);
        }

        @Override
        public <R, T extends Task<ServerScheduler, R>> ScheduledTask<R, T> scheduleIf(Condition condition, T task) {
            return serverScheduleIf(condition, task);
        }

        @Override
        public <R, T extends Task<ServerScheduler, R>> ScheduledTask<R, T> scheduleWhen(Condition condition, T task) {
            return serverScheduleWhen(condition, task);
        }

        void registerToTimerWheel(int index, SchedulingTask<?, Data, ?> task) {
            if (timerWheel[index] == null) {
                timerWheel[index] = new ArrayList<>(2);
            }
            timerWheel[index].add(task);
        }
    }
}
