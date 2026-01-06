package com.outlook.hxsw.settlements.engine.folks.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.id.FolkID;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.folks.FolkSet;
import com.outlook.hxsw.settlements.engine.folks.FolkShareZone;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;
import com.outlook.hxsw.settlements.engine.schedule.*;
import com.outlook.hxsw.settlements.engine.schedule.conditions.WhenChunkLoaded;
import com.outlook.hxsw.settlements.engine.schedule.conditions.WhenChunkSimulated;
import com.outlook.hxsw.settlements.entities.folk.FolkAction;
import com.outlook.hxsw.settlements.entities.folk.FolkActionFailure;
import com.outlook.hxsw.settlements.entities.folk.FolkEntity;
import com.outlook.hxsw.settlements.entities.folk.FolkEntityReference;

import javax.annotation.Nullable;
import java.util.Objects;

public final class Work {
    public FolkID getWorkerID() {
        return workerID;
    }

    /**
     * 表示任务是否已经选择了一个worker执行。
     */
    public boolean isStarted() {
        return !workerID.isEmpty();
    }

    /**
     * 表示任务是否已经被取消或者执行完成。
     * 注意：任务如果在选择了worker之前就取消，就会存在isStarted false而isFinished true的情况
     */
    public boolean isFinished() {
        return waitingJobs.node() == null;
    }

    public void cancel(DataScheduler scheduler) {
        if (scheduledSimTask != null) {
            scheduledSimTask.clear();
        }
        waitingJobs = new JobNodeWithInput<>(null, null);
        callback.onFinish(scheduler, this);
    }

    public FolkID workerID;
    private volatile JobNodeWithInput<?, ?, ?> waitingJobs;

    private @Nullable Folk worker;
    private @Nullable ScheduledTask<DataScheduler, Void> scheduledSimTask;
    private WorkCallback callback = WorkCallback.EMPTY;

    Work(FolkID workerID, JobNodeWithInput<?, ?, ?> waitingJobs) {
        this.workerID = workerID;
        this.waitingJobs = waitingJobs;
    }

    Work(JobNodeWithInput<?, ?, ?> waitingJobs) {
        this(FolkID.empty(), waitingJobs);
    }

    void setWorkerID(FolkID id) {
        workerID = id;
    }

    void run(DataScheduler scheduler) {
        worker = workerID.get(scheduler.data().folks).orElseThrow();
        register(scheduler, waitingJobs);
    }

    void addCallback(WorkCallback callback) {
        this.callback = callback;
    }

    private <I, A, O> void register(DataScheduler scheduler, JobNodeWithInput<I, A, O> current) {
        Objects.requireNonNull(worker);

        if (current.node() == null) {
            callback.onFinish(scheduler, this);
            return;
        }

        Job<I, A, O> job = current.node().job;

        Task<DataScheduler, Void> simTask = s -> {
            O output = job.simulate(worker, s, current.input());
            var next = new JobNodeWithInput<>(output, current.node().next);
            waitingJobs = next;
            register(s, next);
            return null;
        };

        int delay = job.simulationCost(worker, scheduler, current.input());
        if (delay <= 0) { // 无delay的任务不模拟
            simTask.run(scheduler);
            return;
        }

        var scheduledSimTask = this.scheduledSimTask = scheduler.runAfter(delay, simTask);
        var action = job.generateAction(worker, current.input());
        var zone = worker.zone;
        VoidTask<ServerScheduler> actTask = s -> {
            if (scheduledSimTask.clear() == null) { // 停止模拟任务。
                return; // 模拟任务做过了，不需要再做动画了。
            }

            var optEntity = FolkEntityReference.getOrGenerate(s.server(), zone);
            if (optEntity.isEmpty()) {
                return;
            }

            optEntity.get().<A>setAction(
                    action,
                    a -> s.schedule(ds -> { // 注册动作执行完的job afterAction执行
                        O output = job.afterAction(worker, ds, a);
                        var next = new JobNodeWithInput<>(output, current.node().next);
                        waitingJobs = next;
                        register(ds, next);
                    }),
                    () -> { // 注册动作被卸载后...
                        if (!scheduledSimTask.tryUpdate(simTask)) { // 尝试恢复动作
                            s.schedule(simTask); // 无法恢复就立刻执行。
                        }
                    },
                    f -> { // 注册任务失败后
                        s.schedule(this::cancel);
                        if (f == FolkActionFailure.KILLED) {
                            s.schedule((VoidTask<DataScheduler>) ds -> worker.kill());
                        }
                    }
            );
        };
        scheduler.scheduleIf(worker.zone.pos.get().actingCondition(), actTask);
    }

    public static final Codec<Work> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FolkID.CODEC.fieldOf("workerID").forGetter(w -> w.workerID),
            JobNodeWithInput.CODEC.fieldOf("waitingJobs").forGetter(w -> w.waitingJobs)
    ).apply(instance, Work::new));
}
