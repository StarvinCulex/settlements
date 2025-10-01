package com.outlook.hxsw.settlements.engine.schedule.impl;

import com.outlook.hxsw.settlements.engine.schedule.*;

import java.util.function.Predicate;

class SchedulingConditionalBufferedTask<R, S, T extends Task<? super S, R>, C extends Predicate<? super S>> extends SchedulingTask<R, S, T> {
    final C condition;

    SchedulingConditionalBufferedTask(C condition, T task) {
        super(task);
        this.condition = condition;
    }

    @Override
    public void tryExecute(S scheduler) {
        if (condition.test(scheduler)) {
            if (refStatus.compareAndSet(Status.WAITING, Status.EXECUTED)) {
                lastResult = task.run(scheduler);
            }
        }
    }

    @Override
    boolean needsRecycling() {
        return refStatus.get() != Status.WAITING;
    }
}
