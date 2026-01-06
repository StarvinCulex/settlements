package com.outlook.hxsw.settlements.engine.schedule;

import com.outlook.hxsw.settlements.engine.data.SettlementsTable;
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
    
    SettlementsTable table();

    Set<Trigger> triggers();

    <R> ScheduledTask<DataScheduler, R> schedule(Task<DataScheduler, R> task);

    void schedule(VoidTask<DataScheduler> task);

    <R> ScheduledTask<ServerScheduler, R> run(Task<ServerScheduler, R> task);

    void run(VoidTask<ServerScheduler> task);

    <R> ScheduledTask<ServerScheduler, R> runIf(Condition condition, Task<ServerScheduler, R> task);

    void runIf(Condition condition, VoidTask<ServerScheduler> task);

    <R> ScheduledTask<ServerScheduler, R> runWhen(Condition condition, Task<ServerScheduler, R> task);

    void runWhen(Condition condition, VoidTask<ServerScheduler> task);
}
