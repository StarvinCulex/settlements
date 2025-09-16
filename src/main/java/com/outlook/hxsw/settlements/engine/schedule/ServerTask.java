package com.outlook.hxsw.settlements.engine.schedule;

import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record ServerTask(
        Predicate<MinecraftServer> trigger,
        Consumer<Scheduler.Server> task
) {}