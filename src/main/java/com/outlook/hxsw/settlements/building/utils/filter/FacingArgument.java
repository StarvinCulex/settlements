package com.outlook.hxsw.settlements.building.utils.filter;

import com.outlook.hxsw.settlements.engine.buildings.BuildingArgument;
import com.outlook.hxsw.settlements.engine.buildings.BuildingLocation;
import com.outlook.hxsw.settlements.engine.grid.GridSide;

import java.util.Arrays;
import java.util.stream.Stream;

public record FacingArgument<L extends BuildingArgument>(
        GridSide facing,
        L inner
) implements BuildingArgument {
    @Override
    public BuildingLocation<?> location() {
        return inner.location();
    }

    @Override
    public String toString() {
        return String.format("face=%s,%s", facing, inner);
    }

    public static <L extends BuildingArgument>
    Stream<FacingArgument<L>> any(L location) {
        return Arrays.stream(GridSide.values()).map(side -> new FacingArgument<>(side, location));
    }
}
