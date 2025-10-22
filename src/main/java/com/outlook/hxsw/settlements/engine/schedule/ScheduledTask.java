package com.outlook.hxsw.settlements.engine.schedule;


import javax.annotation.Nullable;

public interface ScheduledTask<S, R> {
    /**
     * 获取正在调度的任务
     */
    @Nullable Task<S, R> task();

    /**
     * 尝试替换任务用于调度执行。返回被替换的任务。
     * 如果一个任务已经错过了调度的时机，不可能被执行，那么返回参数本身。
     */
    @Nullable Task<S, R> swap(@Nullable Task<S, R> task);

    default @Nullable Task<S, R> clear() {
        return swap(null);
    }

    default boolean tryUpdate(Task<S, R> task) {
        return swap(task) != task;
    }

    /**
     * 当正在的调度是 expected 的值时，将其替换成newTask，并返回true。
     * 反之返回false，不会做任何修改。
     */
    boolean compareAndSet(@Nullable Task<S, R> expected, @Nullable Task<S, R> newTask);

    default boolean compareAndClear(Task<S, R> expected) {
        return compareAndSet(expected, null);
    }

    default boolean add(Task<S, R> newTask) {
        return compareAndSet(null, newTask);
    }

    /**
     * 返回任务的生命周期是否结束。结束了生命周期的任务无法再被执行。
     */
    boolean isFinished();

    /**
     * 返回上一次是否正常执行。
     * 完成过执行的任务不代表生命周期就结束，其可能还会再次执行。
     * 生命周期结束的任务可能没有执行过，也可能正在执行。
     */
    boolean isExecuted();

    /**
     * 返回上一次执行是否因任务是`null`而取消。
     */
    boolean isCanceled();

    /**
     * 返回任务的上次执行结果（可能是null）。如果没有执行过就返回null。
     */
    @Nullable R result();
}
