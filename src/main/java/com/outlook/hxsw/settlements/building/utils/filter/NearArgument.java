package com.outlook.hxsw.settlements.building.utils.filter;

import com.outlook.hxsw.settlements.engine.buildings.Buildable;
import com.outlook.hxsw.settlements.engine.buildings.BuildingArgument;
import com.outlook.hxsw.settlements.engine.buildings.BuildingLocation;

import java.util.Arrays;
import java.util.Collection;

public record NearArgument<L extends BuildingArgument>(
        Collection<Buildable> neighbors,
        L inner
) implements BuildingArgument {
    @Override
    public BuildingLocation<?> location() {
        return inner.location();
    }

    @Override
    public String toString() {
        return String.format("nbr=%s,%s", Arrays.toString(neighbors.toArray()), inner);
    }
}
