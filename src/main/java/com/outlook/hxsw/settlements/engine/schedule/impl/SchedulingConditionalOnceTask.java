package com.outlook.hxsw.settlements.engine.schedule.impl;

import com.outlook.hxsw.settlements.engine.schedule.Task;

import java.util.function.Predicate;

class SchedulingConditionalOnceTask<R, S, T extends Task<? super S, R>, C extends Predicate<? super S>>
        extends SchedulingConditionalBufferedTask<R, S, T, C>
        implements OnceTask<S>
{
    SchedulingConditionalOnceTask(C condition, T task) {
        super(condition, task);
    }

    @Override
    public void tryExecute(S scheduler) {
        if (condition.test(scheduler)) {
            if (refStatus.compareAndSet(Status.WAITING, Status.EXECUTED)) {
                lastResult = task.run(scheduler);
            }
        } else {
            refStatus.compareAndSet(Status.WAITING, Status.CANCELED);
        }
    }
}
