package com.outlook.hxsw.settlements.building.utils.filter;

import com.outlook.hxsw.settlements.engine.buildings.BuildingArgument;
import com.outlook.hxsw.settlements.engine.buildings.BuildingLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

public record ConnectionArgument<L extends BuildingArgument>(
        Collection<Connection> connections,
        L inner
) implements BuildingArgument {
    @Override
    public BuildingLocation<?> location() {
        return inner.location();
    }

    @Override
    public String toString() {
        return String.format("conn=%s,%s", Arrays.toString(connections.toArray()), inner);
    }

    public static <L extends BuildingArgument> int byCountDesc(ConnectionArgument<L> a, ConnectionArgument<L> b) {
        return Comparator.<ConnectionArgument<L>>comparingInt(c -> -c.connections().size()).compare(a, b);
    }
}
