package com.outlook.hxsw.settlements.building.road;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.agriculture.BuildingFarmstead;
import com.outlook.hxsw.settlements.building.utils.filter.BuildingsFilter;
import com.outlook.hxsw.settlements.building.utils.filter.ConnectionArgument;
import com.outlook.hxsw.settlements.building.utils.ConnectiveBuilding;
import com.outlook.hxsw.settlements.building.utils.ConnectiveBuildingPattern;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.utils.grid.*;

import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BuildingRoad extends ConnectiveBuilding<BuildingRoad.Type> implements Path {
    private BuildingRoad(ConnectionArgument<BuildingLocation<GridCell>> arg) {
        super(Type.DIRT, arg, "road");
    }

    private BuildingRoad(ConnectiveProperties properties)  {
        super(Type.class, properties);
    }

    @Override
    public Type getType() {
        return getBuildingPattern();
    }

    public static final Codec<BuildingRoad> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConnectiveProperties.CODEC.fieldOf("properties").forGetter(BuildingRoad::getProperties)
    ).apply(instance, BuildingRoad::new));

    public enum Type implements Supplier<ConnectiveBuildingPattern>, ConnectiveBuilding.Type<BuildingRoad>,
            BuildingFactory<BuildingRoad, GridCell, ConnectionArgument<BuildingLocation<GridCell>>> {
        DIRT("road", new ConnectiveBuildingPattern("road/dirt", 1, 2)),
        ;

        private static final int HMIN = -1;
        private static final int HMAX = 1;

        private final String name;
        private final ConnectiveBuildingPattern pattern;

        Type(String name, ConnectiveBuildingPattern pattern) {
            this.name = name;
            this.pattern = pattern;
        }

        @Override
        public ConnectiveBuildingPattern get() {
            return pattern;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayKey() {
            return "display.settlements.building.name.road";
        }

        @Override
        public Type getFactory() {
            return this;
        }

        @Override
        public Codec<BuildingRoad> codec() {
            return CODEC;
        }

        @Override
        public GridCellPattern getGridsPattern() {
            return GridCellPattern.of();
        }

        @Override
        public Stream<ConnectionArgument<BuildingLocation<GridCell>>>
        filter(BuildingSet buildingSet, Stream<Grid> requiringGrids) {
            BuildingsFilter filter = new BuildingsFilter(buildingSet);
            return requiringGrids.flatMap(filter.withCell())
                    .map(filter.connectTo(GridSide.SOUTH, connectablePredicate(pointTo -> switch (pointTo.building()) {
                            case BuildingFarmstead f -> f.getType().roadPointedAvailable(pointTo);
                            case Path ignored -> true;
                            default -> false;
                    })));
        }

        @Override
        public BuildingRoad make(ConnectionArgument<BuildingLocation<GridCell>> arg) {
            return new BuildingRoad(arg);
        }

        @Override
        public int connectingHeightMin() {
            return HMIN;
        }

        @Override
        public int connectingHeightMax() {
            return HMAX;
        }
    }
}
