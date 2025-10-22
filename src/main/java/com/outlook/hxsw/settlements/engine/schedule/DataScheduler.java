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

    <R> ScheduledTask<DataScheduler, R> run(Task<DataScheduler, R> task);

    void run(VoidTask<DataScheduler> task);

    <R> ScheduledTask<DataScheduler, R> runAfter(int delayPhases, Task<DataScheduler, R> task);

    void runAfter(int delayPhases, VoidTask<DataScheduler> task);

    <R> ScheduledTask<DataScheduler, R> runWhen(Predicate<DataScheduler> condition, Task<DataScheduler, R> task);

    void runWhen(Predicate<DataScheduler> condition, VoidTask<DataScheduler> task);

    <R> ContinuousScheduledTask<DataScheduler, R> runPeriodically(int phase, int period, Task<DataScheduler, R> task);

    /**
     * 注册一个定时执行的任务。这个注册的任务在当前phase不会执行。
     */
    void runPeriodically(int phase, int period, VoidTask<DataScheduler> task);

    <R> ScheduledTask<ServerScheduler, R> schedule(Task<ServerScheduler, R> task);

    void schedule(VoidTask<ServerScheduler> task);

    <R> ScheduledTask<ServerScheduler, R> scheduleIf(Condition condition, Task<ServerScheduler, R> task);

    void scheduleIf(Condition condition, VoidTask<ServerScheduler> task);

    <R> ScheduledTask<ServerScheduler, R> scheduleWhen(Condition condition, Task<ServerScheduler, R> task);

    void scheduleWhen(Condition condition, VoidTask<ServerScheduler> task);
}
