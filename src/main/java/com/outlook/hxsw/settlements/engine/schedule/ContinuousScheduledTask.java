package com.outlook.hxsw.settlements.engine.schedule;

public interface ContinuousScheduledTask<S, R> extends ScheduledTask<S, R> {
    /**
     * 停止并回收这个永远会循环执行的任务。
     */
    boolean terminate();
}
