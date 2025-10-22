package com.outlook.hxsw.settlements.building.utils;

import com.outlook.hxsw.settlements.engine.buildings.BuildingLocation;
import com.outlook.hxsw.settlements.engine.grid.*;
import com.outlook.hxsw.settlements.engine.schedule.ServerScheduler;
import com.outlook.hxsw.settlements.engine.schedule.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectiveBuildingPattern {
    private final String dirPath;
    private final int groundSurfaceY;
    private final int cornerSize;

    public ConnectiveBuildingPattern(
            String dirPath,
            int groundSurfaceY,
            int cornerSize
    ) {
        this.dirPath = dirPath;
        this.groundSurfaceY = groundSurfaceY;
        this.cornerSize = cornerSize;
    }

    Task<ServerScheduler, Void> generateBuilder(
            BuildingLocation<GridCell> location,
            Map<GridSide, Integer> connections,
            PatternOption... options
    ) {
        BlockPos originPos = new BlockPos(
                location.grids().at().getFromX(),
                location.groundY(),
                location.grids().at().getFromZ()
        );

        List<Task<ServerScheduler, Void>> tasks = new ArrayList<>();
        for (GridSide side : GridSide.values()) {
            tasks.add(makeSides(connections, location.dimension(), originPos, side, options));
            tasks.add(makeCorners(connections, location.dimension(), originPos, side, options));
        }
        tasks.add(makeMain(location.dimension(), originPos, options));

        return scheduler -> {
            tasks.forEach(t -> t.run(scheduler));
            return null;
        };
    }

    private Task<ServerScheduler, Void> makeSides(
            Map<GridSide, Integer> connections,
            ResourceKey<Level> dimension,
            BlockPos originPos,
            GridSide side,
            PatternOption... options
    ) {
        BlockPos offset = switch (side) {
            case SOUTH -> new BlockPos(cornerSize, 0, 8 - cornerSize);
            case WEST -> new BlockPos(0, 0, cornerSize);
            case NORTH -> new BlockPos(cornerSize, 0, 0);
            case EAST -> new BlockPos(8 - cornerSize, 0, cornerSize);
        };

        Integer h = connections.get(side);
        return new StructurePattern(
                dirPath + "/side" + makeHeightSuffix(h),
                groundSurfaceY,
                8 - cornerSize * 2,
                cornerSize
        ).build(dimension, originPos.offset(offset), StructurePattern.getRotation(side), options);
    }

    private Task<ServerScheduler, Void> makeCorners(
            Map<GridSide, Integer> connections,
            ResourceKey<Level> dimension,
            BlockPos originPos,
            GridSide side,
            PatternOption... options
    ) {
            BlockPos offset = switch (side) {
                case SOUTH -> new BlockPos(8 - cornerSize, 0, 8 - cornerSize);
                case WEST -> new BlockPos(0, 0, 8 - cornerSize);
                case NORTH -> new BlockPos(0, 0, 0);
                case EAST -> new BlockPos(8 - cornerSize, 0, 0);
            };

            Integer h1 = connections.get(side);
            Integer h2 = connections.get(side.rotate(GridRelativeSide.LEFT));
            Integer h;
            if (h1 != null && h2 != null) {
                h = Math.max(h1, h2);
            } else {
                h = null;
            }

            return new StructurePattern(
                    dirPath + "/corner" + makeHeightSuffix(h),
                    groundSurfaceY,
                    cornerSize,
                    cornerSize
            ).build(dimension, originPos.offset(offset), StructurePattern.getRotation(side), options);
    }

    private Task<ServerScheduler, Void> makeMain(
            ResourceKey<Level> dimension,
            BlockPos originPos,
            PatternOption... options
    ) {
        return new StructurePattern(
                dirPath + "/main",
                groundSurfaceY,
                8 - cornerSize * 2,
                8 - cornerSize * 2
        ).build(dimension, originPos.offset(cornerSize, 0, cornerSize), Rotation.NONE, options);
    }

    private String makeHeightSuffix(@Nullable Integer h) {
        if (h == null) {
            return "";
        }
        return "_h" + String.valueOf(h).replace('-', '_');
    }
}
