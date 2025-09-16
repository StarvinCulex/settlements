package com.outlook.hxsw.settlements.building.utils.filter;

import com.outlook.hxsw.settlements.engine.buildings.Buildable;
import com.outlook.hxsw.settlements.engine.buildings.BuildingArgument;
import com.outlook.hxsw.settlements.engine.buildings.BuildingLocation;
import com.outlook.hxsw.settlements.engine.buildings.BuildingSet;
import com.outlook.hxsw.settlements.utils.grid.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class BuildingsFilter {
    private final BuildingSet buildingSet;

    public BuildingsFilter(BuildingSet buildingSet) {
        this.buildingSet = buildingSet;
    }

    public Function<Grid, Stream<BuildingLocation<GridCell>>> withCell() {
        return withPattern(GridCellPattern.INSTANCE, 0);
    }

    public <G extends Grids> Function<Grid, Stream<BuildingLocation<G>>> withPattern(
            GridsPattern<G> pattern,
            int maxHeightDelta
    ) {
        return origin -> {
            G grids = pattern.offset(origin);

            // 检查是否被占用
            if (grids.stream().anyMatch(buildingSet.getGridMap()::containsKey)) {
                return Stream.of();
            }

            List<GridTerrain> terrains = grids.stream().map(buildingSet.getTerrainMap()::get).toList();
            // 检查城镇所属地形是否覆盖
            if (terrains.stream().anyMatch(Objects::isNull)) {
                return Stream.of();
            }
            // 获取按频率排序的所有候选高度
            int[] heights = terrains.stream()
                    .map(GridTerrain::groundHeight)
                    .collect(Collectors.groupingBy(h -> h, Collectors.counting()))
                    .entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .mapToInt(Map.Entry::getKey)
                    .toArray();
            // 计算最大值/最小值的极差
            if (heights.length == 0) {
                return Stream.of();
            }
            if (maxHeightDelta < Arrays.stream(heights).max().getAsInt() - Arrays.stream(heights).min().getAsInt()) {
                return Stream.of();
            }

            return Arrays.stream(heights).mapToObj(y -> new BuildingLocation<>(
                    buildingSet.getParent().getDimensionKey(),
                    grids,
                    y
            ));
        };
    }

    public <L extends BuildingArgument>
    Function<FacingArgument<L>, ConnectionArgument<FacingArgument<L>>>
    connectTo(BiPredicate<FacingArgument<L>, Connection> filter) {
        return facingArg -> this.<FacingArgument<L>>connectTo(
                facingArg.facing(),
                (a, c) -> filter.test(facingArg, c)
        ).apply(facingArg);
    }

    public <L extends BuildingArgument>
    Function<L, ConnectionArgument<L>> connectTo(GridSide subjectFacing, BiPredicate<L, Connection> filter) {
        return source -> {
            Grids sourceAt = source.location().grids();
            Map<Buildable, Connection> pointings = new HashMap<>();
            for (GridSide facing : GridSide.values()) {
                Grids nextTo = sourceAt.nextTo(facing);
                for (Grid pointingGrid : nextTo) {
                    Buildable pointedBuilding = buildingSet.getGridMap().get(pointingGrid);
                    if (pointedBuilding == null || pointings.containsKey(pointedBuilding)) {
                        continue;
                    }

                    Function<Grids, Integer> originGetter = switch (facing) {
                        case SOUTH -> Grids::getWest;
                        case WEST -> Grids::getNorth;
                        case NORTH -> Grids::getEast;
                        case EAST -> Grids::getSouth;
                    };
                    int alignment = facing == GridSide.SOUTH || facing == GridSide.WEST ? 1 : -1;
                    alignment *= originGetter.apply(sourceAt) - originGetter.apply(pointedBuilding.getGrids());

                    var p = new Connection(subjectFacing.angle(facing), facing, alignment, pointedBuilding);
                    if (filter.test(source, p)) {
                        pointings.put(pointedBuilding, p);
                    }
                }
            }
            return new ConnectionArgument<>(pointings.values(), source);
        };
    }

    public <L extends BuildingArgument>
    Function<L, NearArgument<L>> near(int gridDistance, Predicate<Buildable> filter) {
        return source -> {
            Grids region = new GridRegion(source.location().grids().center(), gridDistance);
            var neighbors = region.stream()
                    .map(buildingSet.getGridMap()::get)
                    .filter(Objects::nonNull)
                    .distinct()
                    .filter(filter)
                    .toList();

            return new NearArgument<>(neighbors, source);
        };
    }
}
