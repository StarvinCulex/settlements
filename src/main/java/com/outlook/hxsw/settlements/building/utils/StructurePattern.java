package com.outlook.hxsw.settlements.building.utils;

import com.outlook.hxsw.settlements.utils.grid.GridSide;
import com.outlook.hxsw.settlements.utils.grid.GridSize;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class StructurePattern {
    private final String path;
    private final int groundSurfaceY;
    private final int xLen;
    private final int zLen;

    public StructurePattern(String path, int groundSurfaceY, GridSize size) {
        this(path, groundSurfaceY, size.getX(), size.getZ());
    }

    public StructurePattern(String path, int groundSurfaceY, int xLen, int zLen) {
        this.path = path;
        this.groundSurfaceY = groundSurfaceY;
        this.xLen = xLen;
        this.zLen = zLen;
    }

    public Consumer<Scheduler.Server> build(
            ResourceKey<Level> dimension,
            BlockPos northwestCorner,
            Rotation rotation,
            PatternOption... options
    ) {
        return new Builder(dimension, northwestCorner, rotation, options);
    }

    public static Rotation getRotation(GridSide facing) {
        return switch (facing) {
            case SOUTH -> Rotation.NONE;
            case WEST -> Rotation.CLOCKWISE_90;
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
        };
    }

    private void checkBlockLength(StructureTemplate template) {
        if (template.getSize().getX() != xLen) {
            throw new RuntimeException("template is " + template.getSize() + ", expecting x=" + xLen);
        }
        if (template.getSize().getZ() != zLen) {
            throw new RuntimeException("template is " + template.getSize() + ", expecting z=" + zLen);
        }
    }

    private BlockPos getOriginPos(StructureTemplate template, Rotation rotation, BlockPos northwestCorner) {
        Vec3i size = template.getSize();
        return switch (rotation) {
            case NONE -> northwestCorner.offset(0, -groundSurfaceY, 0);
            case CLOCKWISE_90 -> northwestCorner.offset(size.getZ() - 1, -groundSurfaceY, 0);
            case CLOCKWISE_180 -> northwestCorner.offset(size.getX() - 1, -groundSurfaceY, size.getZ() - 1);
            case COUNTERCLOCKWISE_90 -> northwestCorner.offset(0, -groundSurfaceY, size.getX() - 1);
        };
    }

    private ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath("settlements", path);
    }

    static final int UPDATE_FLAG = Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    private class Builder implements Consumer<Scheduler.Server> {
        public Builder(
                ResourceKey<Level> dimension,
                BlockPos northwestCorner,
                Rotation rotation,
                PatternOption... options
        ) {
            this.dimension = dimension;
            this.northwestCorner = northwestCorner;
            this.rotation = rotation;
            this.options = options;
        }

        private final ResourceKey<Level> dimension;
        private final BlockPos northwestCorner;
        private final Rotation rotation;
        private final PatternOption[] options;

        @Override
        public void accept(Scheduler.Server scheduler) {
            build(scheduler.server());
        }

        private void build(MinecraftServer server) {
            ServerLevel level = Objects.requireNonNull(server.getLevel(dimension));
            var template = level.getStructureManager().get(getResourceLocation())
                    .orElseThrow(() -> new RuntimeException("cannot get resource: " + path));
            checkBlockLength(template);
            BlockPos origin = getOriginPos(template, rotation, northwestCorner);
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);

            if (!template.placeInWorld(level, origin, origin, settings, level.getRandom(), UPDATE_FLAG)) {
                throw new RuntimeException("cannot set resource: " + path);
            }
            Stream.concat(Arrays.stream(PREPROCESSING_OPTIONS), Arrays.stream(options))
                    .forEachOrdered(p -> p.apply(level, template, origin, settings));
        }

        private static final PatternOption[] PREPROCESSING_OPTIONS = {
                // minecraft结构的放置只是按一定顺序放置方块。这会有一类问题，比如：
                // 如果一个要放置耕地方块的位置上面有一个方块了，由于放置顺序是从下向上的，那么这个耕地方块在放置的时候就会因为上面有方块而直接变成泥土。
                PatternOption.BlockReplacing.fill(Blocks.DIRT_PATH, Blocks.DIRT_PATH.defaultBlockState()),
                PatternOption.BlockReplacing.fill(Blocks.FARMLAND, Blocks.FARMLAND.defaultBlockState())
        };
    }
}
