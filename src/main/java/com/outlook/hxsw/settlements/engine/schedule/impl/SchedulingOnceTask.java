package com.outlook.hxsw.settlements.engine.schedule.impl;

import com.outlook.hxsw.settlements.engine.schedule.*;

class SchedulingOnceTask<R, S, T extends Task<? super S, R>> extends SchedulingTask<R, S, T> implements OnceTask<S> {
    SchedulingOnceTask(T task) {
        super(task);
    }

    @Override
    public void tryExecute(S scheduler) {
        if (refStatus.compareAndSet(Status.WAITING, Status.EXECUTED)) {
            lastResult = task.run(scheduler);
        }
    }

    @Override
    boolean needsRecycling() {
        return refStatus.get() != Status.WAITING;
    }
}
