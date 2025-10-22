package com.outlook.hxsw.settlements.engine.schedule.conditions;

import com.outlook.hxsw.settlements.engine.grid.Grid;
import com.outlook.hxsw.settlements.engine.grid.Grids;
import com.outlook.hxsw.settlements.engine.schedule.Condition;
import com.outlook.hxsw.settlements.engine.schedule.ServerScheduler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public final class WhenChunkLoaded implements Condition {
    public WhenChunkLoaded(ResourceKey<Level> dimension, ChunkPos... chunkPos) {
        this.dimension = dimension;
        this.chunkPos = Arrays.asList(chunkPos);
    }

    public WhenChunkLoaded(ResourceKey<Level> dimension, Grids grids) {
        this.dimension = dimension;
        this.chunkPos = new HashSet<>();
        for (Grid g : grids) {
            this.chunkPos.add(g.getChunk());
        }
    }

    private final ResourceKey<Level> dimension;
    private final Collection<ChunkPos> chunkPos;

    @Override
    public boolean test(ServerScheduler scheduler) {
        return chunkPos.stream().allMatch(p -> scheduler.server().getLevel(dimension).hasChunk(p.x, p.z));
    }
}
