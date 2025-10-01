package com.outlook.hxsw.settlements.engine.schedule.impl;

import com.outlook.hxsw.settlements.engine.schedule.*;

class SchedulingRepeatingTask<R, S, T extends Task<? super S, R>> extends SchedulingTask<R, S, T> {
    SchedulingRepeatingTask(T task) {
        super(task);
    }

    @Override
    public void tryExecute(S scheduler) {
        if (refStatus.compareAndExchange(Status.WAITING, Status.EXECUTED) != Status.CANCELED) {
            lastResult = task.run(scheduler);
        }
    }

    @Override
    boolean needsRecycling() {
        return refStatus.get() == Status.CANCELED;
    }
}
