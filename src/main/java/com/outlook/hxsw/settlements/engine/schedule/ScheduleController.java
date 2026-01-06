package com.outlook.hxsw.settlements.engine.schedule;

import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.SettlementsTable;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;

public class ScheduleController {
    static final int TIMER_WHEEL_SIZE = 3600;

    public ScheduleController(SettlementsData data, int phase) {
        this.dataSide = new Data(data, phase);
    }

    public DataAccessor getData() {
        return new DataAccessor();
    }

    public void consumeServerTasks(MinecraftServer server) {
        ServerAdapter adapter = new ServerAdapter(server);
        adapter.consumeFromQueue();
        adapter.consumeFromBuffer();
    }

    public void startDataSide() {
        dataSide.start();
    }

    public void stopDataSide() {
        dataSide.stop();
    }

    public void dataSideTick() {
        dataTaskQueue.add((OnceTask<Data>)Data::tick);
    }

    protected DataScheduler getDataSide() {
        return Objects.requireNonNull(dataSide);
    }

    public final void dataSchedule(VoidTask<DataScheduler> task) {
        dataTaskQueue.add(OnceTask.from(task));
    }

    public final SettlementsTable getTable() {
        return dataSide.data.syncTable;
    }

    public final <R> ScheduledTask<DataScheduler, R> dataSchedule(Task<DataScheduler, R> task) {
        var t = new AsyncScheduledOnceTask<>(task);
        dataTaskQueue.add(t);
        return t;
    }

    public final void dataScheduleWhen(Predicate<DataScheduler> condition, VoidTask<DataScheduler> task) {
        dataTaskQueue.add(s -> {
            boolean test = condition.test(s);
            if (test) {
                task.run(s);
            }
            return test;
        });
    }

    public final <R> ScheduledTask<DataScheduler, R>
    dataScheduleWhen(Predicate<DataScheduler> condition, Task<DataScheduler, R> task) {
        var t = new AsyncScheduledConditionalBufferedTask<>(condition, task);
        dataTaskQueue.add(t);
        return t;
    }

    public final void serverSchedule(VoidTask<ServerScheduler> task) {
        serverTaskQueue.add(OnceTask.from(task));
    }

    public final <R> ScheduledTask<ServerScheduler, R> serverSchedule(Task<ServerScheduler, R> task) {
        var t = new AsyncScheduledOnceTask<>(task);
        serverTaskQueue.add(t);
        return t;
    }

    public final <R> ScheduledTask<ServerScheduler, R>
    serverScheduleIf(Condition condition, Task<ServerScheduler, R> task) {
        var t = new AsyncScheduledConditionalOnceTask<>(condition, task);
        serverTaskQueue.add(t);
        return t;
    }

    public final void serverScheduleIf(Condition condition, VoidTask<ServerScheduler> task) {
        serverTaskQueue.add(ss -> {
            if (condition.test(ss)) {
                task.run(ss);
            }
        });
    }

    public final void serverScheduleWhen(Condition condition, VoidTask<ServerScheduler> task) {
        registerServerConditionalTask(condition, OnceTask.from(task));
    }

    public final <R> ScheduledTask<ServerScheduler, R>
    serverScheduleWhen(Condition condition, Task<ServerScheduler, R> task) {
        var scheduling = new AsyncScheduledConditionalBufferedTask<>(condition, task);
        registerServerConditionalTask(condition, scheduling);
        return scheduling;
    }

    private void registerServerConditionalTask(Condition condition, STask<ServerScheduler> task) {
        var triggers = condition.triggers();
        if (triggers.isEmpty()) {
            serverTriggerlessTaskBuffer.add(task);
        }
        for (Trigger trigger : triggers) {
            var queue = serverTaskBuffer.computeIfAbsent(trigger, x -> new ConcurrentLinkedQueue<>());
            queue.add(task);
        }
    }

    private final Data dataSide;

    private final BlockingQueue<STask<? super Data>> dataTaskQueue = new LinkedBlockingQueue<>();
    private final Queue<OnceTask<? super ServerAdapter>> serverTaskQueue = new ConcurrentLinkedQueue<>();
    private final Map<Trigger, Queue<STask<? super ServerAdapter>>> serverTaskBuffer = new ConcurrentHashMap<>();
    private final Queue<STask<? super ServerAdapter>> serverTriggerlessTaskBuffer = new ConcurrentLinkedQueue<>();

    public final class DataAccessor implements Supplier<SettlementsData>, AutoCloseable {
        private DataAccessor() {
            dataSide.needPause = true;
            dataSide.dataLock.lock();
            dataSide.needPause = false;
        }

        public int phase() {
            return dataSide.getPhase();
        }

        @Override
        public SettlementsData get() {
            return dataSide.data;
        }

        @Override
        public void close() {
            dataSide.dataLock.unlock();
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

        private void consumeBuffer(Queue<STask<? super ServerAdapter>> buffer) {
            if (buffer == null) {
                return;
            }
            buffer.removeIf(t -> t.tryExecute(this));
        }

        @Override
        public MinecraftServer server() {
            return server;
        }

        @Override
        public SettlementsTable table() {
            return dataSide.data.syncTable; // ?
        }

        @Override
        public Set<Trigger> triggers() {
            return triggers;
        }

        @Override
        public <R> ScheduledTask<DataScheduler, R> schedule(Task<DataScheduler, R> task) {
            return dataSchedule(task);
        }

        @Override
        public void schedule(VoidTask<DataScheduler> task) {
            dataSchedule(task);
        }

        @Override
        public <R> ScheduledTask<ServerScheduler, R> run(Task<ServerScheduler, R> task) {
            return serverSchedule(task);
        }

        @Override
        public void run(VoidTask<ServerScheduler> task) {
            serverSchedule(task);
        }

        @Override
        public <R> ScheduledTask<ServerScheduler, R> runIf(Condition condition, Task<ServerScheduler, R> task) {
            return serverScheduleIf(condition, task);
        }

        @Override
        public void runIf(Condition condition, VoidTask<ServerScheduler> task) {
            serverScheduleIf(condition, task);
        }

        @Override
        public <R> ScheduledTask<ServerScheduler, R> runWhen(Condition condition, Task<ServerScheduler, R> task) {
            return serverScheduleWhen(condition, task);
        }

        @Override
        public void runWhen(Condition condition, VoidTask<ServerScheduler> task) {
            serverScheduleWhen(condition, task);
        }
    }

    final class Data implements DataScheduler {
        private final SettlementsData data;
        private int phase;

        private final ReentrantLock dataLock = new ReentrantLock(true);
        private volatile boolean needPause = false;
        private volatile boolean needStop = false;

        private final ArrayList<STask<? super Data>> localBuffer = new ArrayList<>();
        @SuppressWarnings("unchecked")
        private final ArrayList<STask<? super Data>>[] timerWheel = new ArrayList[TIMER_WHEEL_SIZE];
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
                super("SettlementsDataSideRunner");
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
            consumeFromBuffer(localBuffer, localBuffer::size);
        }

        private void executeFromWheel() throws InterruptedException {
            var tasks = timerWheel[phase];
            if (tasks == null) {
                return;
            }
            int size = tasks.size();
            // 我们不希望遍历新插入的元素。
            consumeFromBuffer(tasks, () -> size);
        }

        // 这个方法存在是因为在tryExecute调用的时候可能会向buffer中追加元素。而我还希望这些追加的元素能在此次遍历中得到。
        private void consumeFromBuffer(ArrayList<STask<? super Data>> buffer, Supplier<Integer> range)
                throws InterruptedException
        {
            int src = 0, dest = 0;
            while (src < range.get()) {
                STask<? super Data> task = buffer.get(src++);
                if (!task.tryExecute(this)) {
                    buffer.set(dest++, task);
                }
                nap();
            }
            buffer.subList(dest, range.get()).clear();
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
        public <R> ScheduledTask<DataScheduler, R> run(Task<DataScheduler, R> task) {
            var t = new AsyncScheduledOnceTask<>(task);
            localBuffer.add(t);
            return t;
        }

        @Override
        public void run(VoidTask<DataScheduler> task) {
            localBuffer.add(OnceTask.from(task));
        }

        @Override
        public <R> ScheduledTask<DataScheduler, R> runAfter(int delayPhases, Task<DataScheduler, R> task) {
            var t = new AsyncScheduledOnceTask<>(task);
            registerDelayTask(delayPhases, t);
            return t;
        }

        @Override
        public void runAfter(int delayPhases, VoidTask<DataScheduler> task) {
            registerDelayTask(delayPhases, OnceTask.from(task));
        }

        @Override
        public <R> ContinuousScheduledTask<DataScheduler, R> runPeriodically(int phase, int period, Task<DataScheduler, R> task) {
            var t = new AsyncScheduledRepeatingTask<>(task);
            registerPeriodicTask(phase, period, t);
            return t;
        }

        @Override
        public void runPeriodically(int phase, int period, VoidTask<DataScheduler> task) {
            registerPeriodicTask(phase, period, s -> {
                task.run(s);
                return false;
            });
        }

        private void registerPeriodicTask(int phase, int period, STask<? super Data> task) {
            phase = (phase % period + period) % period;
            for (int i = phase; i < timerWheel.length; i += period) {
                registerToTimerWheel(i, task);
            }
        }

        @Override
        public <R> ScheduledTask<DataScheduler, R> runWhen(Predicate<DataScheduler> condition, Task<DataScheduler, R> task) {
            var t = new AsyncScheduledConditionalBufferedTask<>(condition, task);
            localBuffer.add(t);
            return t;
        }

        @Override
        public void runWhen(Predicate<DataScheduler> condition, VoidTask<DataScheduler> task) {
            localBuffer.add(s -> {
                boolean test = condition.test(s);
                if (test) {
                    task.run(s);
                }
                return test;
            });
        }

        private void registerDelayTask(int delayPhases, OnceTask<? super Data> task) {
            if (delayPhases >= TIMER_WHEEL_SIZE || delayPhases <= 0) {
                throw new IllegalArgumentException("delayPhases >= WHEEL_SIZE or delayPhases <= 0");
            }
            registerToTimerWheel(phase + delayPhases, task);
        }

        @Override
        public <R> ScheduledTask<ServerScheduler, R> schedule(Task<ServerScheduler, R> task) {
            return serverSchedule(task);
        }

        @Override
        public void schedule(VoidTask<ServerScheduler> task) {
            serverSchedule(task);
        }

        @Override
        public <R> ScheduledTask<ServerScheduler, R> scheduleIf(Condition condition, Task<ServerScheduler, R> task) {
            return serverScheduleIf(condition, task);
        }

        @Override
        public void scheduleIf(Condition condition, VoidTask<ServerScheduler> task) {
            serverScheduleIf(condition, task);
        }

        @Override
        public <R> ScheduledTask<ServerScheduler, R> scheduleWhen(Condition condition, Task<ServerScheduler, R> task) {
            return serverScheduleWhen(condition, task);
        }

        @Override
        public void scheduleWhen(Condition condition, VoidTask<ServerScheduler> task) {
            serverScheduleWhen(condition, task);
        }

        void registerToTimerWheel(int index, STask<? super Data> task) {
            if (timerWheel[index] == null) {
                timerWheel[index] = new ArrayList<>(2);
            }
            timerWheel[index].add(task);
        }
    }
}

interface STask<S> {
    /**
     * @return 需要回收返回true，反之返回false。
     */
    boolean tryExecute(S scheduler);
}

@FunctionalInterface
interface OnceTask<S> extends STask<S> {
    void execute(S scheduler);
    default boolean tryExecute(S scheduler) {
        execute(scheduler);
        return true;
    }

    static <S> OnceTask<S> from(VoidTask<? super S> task) {
        return task::run;
    }
}

abstract class AsyncScheduledTask<S, R> implements ScheduledTask<S, R> {
    @SuppressWarnings("unused")
    sealed interface TaskSlot<S, R> permits Task, FinishedMark{}
    static final class FinishedMark<S, R> implements TaskSlot<S, R> {
        private FinishedMark() {}
        static final FinishedMark<?, ?> INSTANCE = new FinishedMark<>();
        @SuppressWarnings("unchecked")
        static <S, R> FinishedMark<S, R> instance() {
            return (FinishedMark<S, R>) INSTANCE;
        }
    }

    record Result<R> (@Nullable R value, Status status) {
        enum Status {
            UNSET,
            EXECUTED,
            NULL_TASK,
        }
    }

    final AtomicReference<TaskSlot<S, R>> scheduledTask; // 内容是null表示没有任务。
    final AtomicReference<Result<R>> result;

    AsyncScheduledTask(Task<S, R> task) {
        this.scheduledTask = new AtomicReference<>(Objects.requireNonNull(task));
        result = new AtomicReference<>(new Result<>(null, Result.Status.UNSET));
    }

    public final @Nullable Task<S, R> task() {
        return scheduledTask.get() instanceof Task<S, R> task ? task : null;
    }

    public final @Nullable Task<S, R> swap(@Nullable Task<S, R> newTask) {
        Objects.requireNonNull(newTask);
        while (true) {
            var current = scheduledTask.get();
            if (current == FinishedMark.INSTANCE) {
                return newTask;
            }
            if (scheduledTask.weakCompareAndSetVolatile(current, newTask)) {
                return current instanceof Task<S, R> task ? task : null;
            }
        }
    }

    @Override
    public final boolean compareAndSet(@Nullable Task<S, R> expected, @Nullable Task<S, R> newTask) {
        return scheduledTask.compareAndSet(expected, newTask);
    }

    @Override
    public final boolean isFinished() {
        return scheduledTask.getAcquire() == FinishedMark.INSTANCE;
    }

    @Override
    public final boolean isExecuted() {
        return result.getAcquire().status == Result.Status.EXECUTED;
    }

    @Override
    public final boolean isCanceled() {
        return result.getAcquire().status == Result.Status.NULL_TASK;
    }

    @Override
    public final @Nullable R result() {
        return result.getAcquire().value;
    }

    final void rawExecute(S scheduler) {
        var slot = scheduledTask.getAndSet(FinishedMark.instance());
        rawExecuteSlot(scheduler, slot);
    }

    final void rawExecuteSlot(S scheduler, @Nullable TaskSlot<S, R> slot) {
        Result<R> r = switch (slot) {
            case null -> new Result<>(null, Result.Status.NULL_TASK);
            case Task<S, R> task -> new Result<>(task.run(scheduler), Result.Status.EXECUTED);
            case AsyncScheduledTask.FinishedMark<S, R> ignored -> throw new RuntimeException();
        };
        result.lazySet(r);
    }
}

class AsyncScheduledOnceTask<S, R> extends AsyncScheduledTask<S, R> implements OnceTask<S> {
    AsyncScheduledOnceTask(Task<S, R> task) {
        super(task);
    }

    @Override
    public void execute(S scheduler) {
        rawExecute(scheduler);
    }
}

class AsyncScheduledConditionalBufferedTask<S, R, C extends Predicate<? super S>>
        extends AsyncScheduledTask<S, R>
        implements STask<S>
{
    final C condition;

    AsyncScheduledConditionalBufferedTask(C condition, Task<S, R> task) {
        super(task);
        this.condition = condition;
    }

    @Override
    public boolean tryExecute(S scheduler) {
        boolean test = condition.test(scheduler);
        if (test) {
            rawExecute(scheduler);
        }
        return test;
    }
}

class AsyncScheduledConditionalOnceTask<S, R, C extends Predicate<? super S>>
        extends AsyncScheduledConditionalBufferedTask<S, R, C>
        implements OnceTask<S>
{
    AsyncScheduledConditionalOnceTask(C condition, Task<S, R> task) {
        super(condition, task);
    }

    @Override
    public void execute(S scheduler) {
        if (!super.tryExecute(scheduler)) {
            scheduledTask.set(FinishedMark.instance());
            result.lazySet(new Result<>(null, Result.Status.NULL_TASK));
        }
    }
}

class AsyncScheduledRepeatingTask<S, R> extends AsyncScheduledTask<S, R>
        implements STask<S>, ContinuousScheduledTask<S, R> {
    AsyncScheduledRepeatingTask(Task<S, R> task) {
        super(task);
    }

    @Override
    public boolean tryExecute(S scheduler) {
        var slot = scheduledTask.get();
        if (slot == FinishedMark.INSTANCE) {
            return true;
        }
        rawExecuteSlot(scheduler, slot);
        return false;
    }

    @Override
    public boolean terminate() {
        return scheduledTask.getAndSet(FinishedMark.instance()) != FinishedMark.INSTANCE;
    }
}