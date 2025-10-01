package com.outlook.hxsw.settlements.engine.schedule;

import net.minecraft.server.MinecraftServer;

import java.util.Set;

/**
 * 服务端主线程调度器接口。
 */
public interface ServerScheduler {
    /**
     * 获取底层的MinecraftServer对象。
     */
    MinecraftServer server();

    Set<Trigger> triggers();

    <R, T extends Task<DataScheduler, R>>
    ScheduledTask<R, T> schedule(T task);

    <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> run(T task);

    <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> runIf(Condition condition, T task);

    <R, T extends Task<ServerScheduler, R>>
    ScheduledTask<R, T> runWhen(Condition condition, T task);
}
