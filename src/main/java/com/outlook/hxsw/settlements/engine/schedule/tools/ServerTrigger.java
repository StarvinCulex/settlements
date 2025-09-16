package com.outlook.hxsw.settlements.engine.schedule.tools;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ServerTrigger {
    private ServerTrigger() {}

    public static NextTick INSTANCE = new NextTick();
    public static final class NextTick implements Predicate<MinecraftServer> {
        private NextTick() {}

        @Override
        public boolean test(MinecraftServer server) {
            return true;
        }
    }

    public static class WhenChunkLoaded implements Predicate<MinecraftServer> {
        public WhenChunkLoaded(ResourceKey<Level> dimension, Stream<ChunkPos> chunkPos) {
            this.dimension = dimension;
            this.chunkPos = chunkPos;
        }

        public WhenChunkLoaded(ResourceKey<Level> dimension, ChunkPos chunkPos) {
            this.dimension = dimension;
            this.chunkPos = Stream.of(chunkPos);
        }

        private final ResourceKey<Level> dimension;
        private final Stream<ChunkPos> chunkPos;

        @Override
        public boolean test(MinecraftServer server) {
            return chunkPos.allMatch(p -> server.getLevel(dimension).hasChunk(p.x, p.z));
        }
    }
}
