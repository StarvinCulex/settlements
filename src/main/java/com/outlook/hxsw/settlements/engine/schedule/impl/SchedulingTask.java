package com.outlook.hxsw.settlements.engine.schedule.impl;

import com.outlook.hxsw.settlements.engine.schedule.ScheduledTask;
import com.outlook.hxsw.settlements.engine.schedule.Task;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

abstract class SchedulingTask<R, S, T extends Task<? super S, R>> implements ScheduledTask<R, T> {
    final T task;
    final AtomicReference<Status> refStatus = new AtomicReference<>(Status.WAITING);
    volatile @Nullable R lastResult = null;
    enum Status {
        WAITING,
        EXECUTED,
        CANCELED,
    }

    SchedulingTask(T task) {
        this.task = task;
    }

    public abstract void tryExecute(S scheduler); // 是public是为了兼容OnceTask接口
    abstract boolean needsRecycling();

    @Override
    public final T task() {
        return task;
    }

    @Override
    public final @Nullable R result() {
        return lastResult;
    }

    @Override
    public final boolean isWaiting() {
        return refStatus.get() == Status.WAITING;
    }

    @Override
    public final boolean isExecuted() {
        return refStatus.get() == Status.EXECUTED;
    }

    @Override
    public final boolean isCanceled() {
        return refStatus.get() == Status.CANCELED;
    }

    /**
     * 尝试取消尚未触发且未取消的任务。
     * @return 任务未触发且未取消返回true，反之返回false
     */
    @Override
    public final boolean cancel(R result) {
        boolean success = cancel();
        if (success) {
            lastResult = result;
        }
        return success;
    }

    @Override
    public final boolean cancel() {
        return refStatus.compareAndSet(Status.WAITING, Status.CANCELED);
    }
}
