package com.outlook.hxsw.settlements.engine.folks.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.id.FolkID;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.folks.FolkSet;
import com.outlook.hxsw.settlements.engine.schedule.*;

import javax.annotation.Nullable;

public final class Work {
    public FolkID getWorkerID() {
        return workerID;
    }

    public boolean isStarted() {
        return workerID.isEmpty();
    }

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
    private volatile JobNodeWithInput<?, ?> waitingJobs;

    private @Nullable Folk worker;
    private @Nullable ScheduledTask<DataScheduler, Void> scheduledSimTask;
    private WorkCallback callback = WorkCallback.EMPTY;

    Work(FolkID workerID, JobNodeWithInput<?, ?> waitingJobs) {
        this.workerID = workerID;
        this.waitingJobs = waitingJobs;
    }

    Work(JobNodeWithInput<?, ?> waitingJobs) {
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

    private <I, O> void register(DataScheduler scheduler, JobNodeWithInput<I, O> current) {
        if (current.node() == null) {
            callback.onFinish(scheduler, this);
            return;
        }

        Task<DataScheduler, Void> task = s -> {
            O output = current.node().job.simulate(worker, s, current.input());
            var next = new JobNodeWithInput<>(output, current.node().next);
            waitingJobs = next;
            register(s, next);
            return null;
        };

        int delay = current.node().job.simulationCost(worker, scheduler, current.input());
        if (delay <= 0) {
            task.run(scheduler);
        } else {
            scheduledSimTask = scheduler.runAfter(delay, task);
        }
    }

    public static final Codec<Work> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FolkID.CODEC.fieldOf("workerID").forGetter(w -> w.workerID),
            JobNodeWithInput.CODEC.fieldOf("waitingJobs").forGetter(w -> w.waitingJobs)
    ).apply(instance, Work::new));
}
