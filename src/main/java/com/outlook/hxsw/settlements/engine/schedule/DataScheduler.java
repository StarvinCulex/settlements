package com.outlook.hxsw.settlements.engine.schedule;

import com.outlook.hxsw.settlements.engine.data.SettlementsData;

import java.util.function.Predicate;

/**
 * 数据线程调度器接口。
 * 负责SettlementsData相关的调度与定时任务处理。
 */
public interface DataScheduler {
    /**
     * 获取当前的数据对象。
     */
    SettlementsData data();

    int getPhase();

    <R, T extends Task<DataScheduler, R>>
    ScheduledTask<R, T> run(T task);

    <R, T extends Task<DataScheduler, R>>
    ScheduledTask<R, T> runAfter(int delayPhases, T task);

    <R, T extends Task<DataScheduler, R>>
    ScheduledTask<R, T> runWhen(Predicate<DataScheduler> condition, T task);

    /**
     * 注册一个定时执行的任务。这个注册的任务在当前phase不会执行。
     */
    <R, T extends Task<DataScheduler, R>>
    ScheduledTask<R, T> runPeriodically(int phase, int period, T task);

    <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> schedule(T task);

    <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> scheduleIf(Condition condition, T task);

    <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> scheduleWhen(Condition condition, T task);
}
