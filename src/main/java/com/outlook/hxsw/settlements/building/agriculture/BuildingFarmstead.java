package com.outlook.hxsw.settlements.building.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.road.Path;
import com.outlook.hxsw.settlements.building.utils.StructurePattern;
import com.outlook.hxsw.settlements.building.utils.filter.*;
import com.outlook.hxsw.settlements.commercial.goods.Sellable;
import com.outlook.hxsw.settlements.commercial.market.Shop;
import com.outlook.hxsw.settlements.commercial.market.WithShop;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.data.utils.Child;
import com.outlook.hxsw.settlements.engine.folks.job.WorkGroup;
import com.outlook.hxsw.settlements.engine.grid.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BuildingFarmstead extends HarvesterBuilding<BuildingFarmstead.Type>
        implements WithShop {
    public static final int MASTERING_DISTANCE = 16;
    public static final int INCOMPATIBLE_DISTANCE = MASTERING_DISTANCE * 2 + 4;

    private final Shop shop;
    @Child public final WorkGroup<BuildingFarmstead> workGroup;

    private static final List<Sellable> GOODS = Arrays.stream(BuildingFarmField.Type.values())
            .flatMap(t -> t.harvest().keySet().stream()).toList();

    private BuildingFarmstead(
            int id,
            Type pattern,
            ConnectionArgument<FacingArgument<BuildingLocation<GridRegion>>> location
    ) {
        super(id, pattern, location.inner(), pattern.getName());
        this.shop = new Shop(GOODS);
        this.workGroup = new WorkGroup<>(BuildingFarmstead.class);
    }

    private BuildingFarmstead(BasicProperties properties, Shop shop, WorkGroup<BuildingFarmstead> workGroup) {
        super(Type.class, properties);
        this.shop = shop;
        this.workGroup = workGroup;
    }

    @Override
    public Type getType() {
        return getBuildingPattern();
    }

    @Override
    public Shop getStock() {
        return shop;
    }

    @Override
    public WorkGroup<BuildingFarmstead> getWorkGroup() {
        return workGroup;
    }

    public static Codec<BuildingFarmstead> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BasicProperties.CODEC.fieldOf("properties").forGetter(BuildingFarmstead::getProperties),
            Shop.CODEC.fieldOf("shop").forGetter(BuildingFarmstead::getStock),
            WorkGroup.codec(BuildingFarmstead.class).fieldOf("workGroup").forGetter(BuildingFarmstead::getWorkGroup)
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
        public BuildingFarmstead make(int id, ConnectionArgument<FacingArgument<BuildingLocation<GridRegion>>> args) {
            return new BuildingFarmstead(id, this, args);
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
