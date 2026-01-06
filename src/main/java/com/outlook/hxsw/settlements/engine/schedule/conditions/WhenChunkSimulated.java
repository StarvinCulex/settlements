package com.outlook.hxsw.settlements.engine.schedule.conditions;

import com.outlook.hxsw.settlements.engine.schedule.Condition;
import com.outlook.hxsw.settlements.engine.schedule.ServerScheduler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class WhenChunkSimulated implements Condition {
    public final ResourceKey<Level> dimension;
    public final ChunkPos chunkPos;

    public WhenChunkSimulated(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        this.dimension = dimension;
        this.chunkPos = chunkPos;
    }

    @Override
    public boolean test(ServerScheduler scheduler) {
        ServerLevel level = scheduler.server().getLevel(dimension);
        if (level == null) {
            return false;
        }

        long chunkLong = chunkPos.toLong();
        return level.getChunkSource().chunkMap.getDistanceManager().inBlockTickingRange(chunkLong);
    }
}
