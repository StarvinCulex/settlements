package com.outlook.hxsw.settlements.engine.schedule;


import javax.annotation.Nullable;

public interface ScheduledTask<R, T extends Task<?, R>> {
    T task();

    /**
     * 小心！存在一种状态：
     * 任务刚刚被决定执行，这时它的状态是executed。
     * 但是还没执行完，所以读结果时result()还在返回null。
     */
    @Nullable R result();

    boolean isWaiting();
    boolean isExecuted();
    boolean isCanceled();
    /**
     * 尝试取消尚未触发且未取消的任务。
     * @return 任务未触发且未取消返回true，反之返回false
     */
    boolean cancel(@Nullable R result);

    boolean cancel();
}
