package com.outlook.hxsw.settlements.engine.schedule;

import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.Set;

public interface Trigger extends Condition {
    @Override
    default boolean test(ServerScheduler schedule) {
        return schedule.triggers().contains(this);
    }

    @Override
    default Set<Trigger> triggers() {
        return Collections.singleton(this);
    }

    static Set<Trigger> generateTriggers(MinecraftServer server) {
        return Set.of();
    }
}
