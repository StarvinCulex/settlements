package com.outlook.hxsw.settlements.engine.schedule;

import java.util.function.Consumer;

/**
 * 表示数据线程(Scheduler.Data)每隔一段周期要执行的任务
 * @param phase 触发偏移量
 * @param period 执行周期
 * @param task 执行的任务
 */
public record SidecarTimer(int phase, int period, Consumer<Scheduler.Data> task) {
    public SidecarTimer {
        if (period <= 0) {
            throw new IllegalArgumentException("SidecarTimer period <= 0");
        }
        if (task == null) {
            throw new IllegalArgumentException("SidecarTimer task is null");
        }
        phase = ((phase % period) + period) % period;
    }
}