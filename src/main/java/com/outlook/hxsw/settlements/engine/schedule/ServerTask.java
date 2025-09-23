package com.outlook.hxsw.settlements.engine.schedule;

import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 代表一个将在服务器主线程调度器(Scheduler.Server)上触发和执行的任务。
 @param trigger 触发该任务的前置条件
 @param task 具体任务操作。
 */
public record ServerTask(
        Predicate<MinecraftServer> trigger,
        Consumer<Scheduler.Server> task
) {}