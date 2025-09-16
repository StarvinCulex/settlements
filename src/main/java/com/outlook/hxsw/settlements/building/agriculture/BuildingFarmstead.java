package com.outlook.hxsw.settlements.building.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.road.Path;
import com.outlook.hxsw.settlements.building.utils.BasicBuilding;
import com.outlook.hxsw.settlements.building.utils.StructurePattern;
import com.outlook.hxsw.settlements.building.utils.filter.*;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.utils.grid.*;

import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BuildingFarmstead extends BasicBuilding<BuildingFarmstead.Type> implements Harvester {
    public static final int MASTERING_DISTANCE = 16;
    public static final int INCOMPATIBLE_DISTANCE = MASTERING_DISTANCE * 2 + 4;

    private BuildingFarmstead(Type pattern, ConnectionArgument<FacingArgument<BuildingLocation<GridRegion>>> location) {
        super(pattern, location.inner(), pattern.getName());
    }

    private BuildingFarmstead(BasicProperties properties) {
        super(Type.class, properties);
    }

    @Override
    public Type getType() {
        return getBuildingPattern();
    }

    @Override
    public void harvest() {

    }

    public static Codec<BuildingFarmstead> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BasicProperties.CODEC.fieldOf("properties").forGetter(BuildingFarmstead::getProperties)
    ).apply(instance, BuildingFarmstead::new));

    public enum Type implements Supplier<StructurePattern>,
            BuildingType<BuildingFarmstead>,
            BuildingFactory<BuildingFarmstead, GridRegion, ConnectionArgument<FacingArgument<BuildingLocation<GridRegion>>>> {
            WOODEN("farmstead", GridSize.of(2, 2), "farmstead/wooden", 2),
        ;


        Type(String name, GridSize size, String resourcePath, int groundSurfaceY) {
            this.name = name;
            this.size = size;
            this.pattern = new StructurePattern(resourcePath, groundSurfaceY, size);
        }

        private final String name;
        private final GridSize size;
        private final StructurePattern pattern;

        public boolean roadPointedAvailable(Connection connection) {
            return connection.building().getType().equals(this)
                    && connection.pointedSide() == GridRelativeSide.FRONT
                    && connection.alignment() == 1;
        }

        boolean connectionFilter(FacingArgument<BuildingLocation<GridRegion>> ignored, Connection connection) {
            return connection.building() instanceof Path
                    && connection.relativeSide() == GridRelativeSide.FRONT
                    && connection.alignment() == -1;
        }

        @Override
        public GridSize getGridsPattern() {
            return size;
        }

        @Override
        public Stream<ConnectionArgument<FacingArgument<BuildingLocation<GridRegion>>>> filter(BuildingSet buildingSet, Stream<Grid> requiringGrids) {
            BuildingsFilter filter = new BuildingsFilter(buildingSet);
            return requiringGrids.flatMap(filter.withPattern(size, 1))
                    .flatMap(
                            l -> FacingArgument.any(l)
                                    // 附近不能有同类型邻居。
                                    .filter(
                                            a -> filter.near(INCOMPATIBLE_DISTANCE,
                                                    b -> b instanceof BuildingFarmstead).apply(a).neighbors().isEmpty()
                                    ).map(filter.connectTo(this::connectionFilter))
                                    .sorted(ConnectionArgument::byCountDesc)
                    );
        }

        @Override
        public BuildingFarmstead make(ConnectionArgument<FacingArgument<BuildingLocation<GridRegion>>> args) {
            return new BuildingFarmstead(this, args);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayKey() {
            return "display.settlements.building.name.farmstead";
        }

        @Override
        public Type getFactory() {
            return this;
        }

        @Override
        public Codec<BuildingFarmstead> codec() {
            return CODEC;
        }

        @Override
        public StructurePattern get() {
            return pattern;
        }
    }
}
