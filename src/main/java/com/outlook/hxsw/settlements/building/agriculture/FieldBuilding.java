package com.outlook.hxsw.settlements.building.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.utils.*;
import com.outlook.hxsw.settlements.building.utils.filter.*;
import com.outlook.hxsw.settlements.commercial.goods.Sellable;
import com.outlook.hxsw.settlements.commercial.market.Stockpile;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.data.id.ID;
import com.outlook.hxsw.settlements.engine.folks.pos.InBuilding;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.engine.grid.*;
import com.outlook.hxsw.settlements.folk.job.HarvestFromField;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class FieldBuilding<P extends Enum<P> & Supplier<ConnectiveBuildingPattern>> extends ConnectiveBuilding<P> {
    private boolean timerIsSet = false;

    protected FieldBuilding(
            int id,
            P variant,
            NearArgument<ConnectionArgument<BuildingLocation<GridCell>>> arg,
            String name
    ) {
        super(variant.getDeclaringClass(), new FieldProperty(
                id,
                ID.of((HarvesterBuilding<?>) arg.neighbors().stream().findFirst().orElseThrow()),
                arg.inner().inner(),
                name,
                variant.name()
        ));
        connectAll(arg.inner().connections());
    }

    protected FieldBuilding(Class<P> variantClass, FieldProperty properties) {
        super(variantClass, properties);
    }

    public final int getGrowStage() {
        return getProperties().growStage;
    }

    public final InBuilding harvestPos() {
        return new InBuilding(this, getType().harvestBlocks());
    }

    public final Stockpile harvest() {
        Stockpile stockpile = new Stockpile();
        if (getGrowStage() >= getType().growMaxStage()) {
            stockpile.store(getType().harvest());
            getProperties().growStage = 0;
        }
        return stockpile;
    }

    @Override
    public abstract Type<? extends FieldBuilding<P>> getType();

    @Override
    public FieldProperty getProperties() {
        return (FieldProperty) super.getProperties();
    }

    @Override
    protected void onRegistering(DataScheduler scheduler) {
        super.onRegistering(scheduler);
        if (timerIsSet) {
            if (getProperties().timerPhase == null) {
                getProperties().timerPhase = scheduler.getPhase();
            }
            timerIsSet = true;
            scheduler.runPeriodically(getProperties().timerPhase, getType().growTickCost(), this::tick);
        }
    }

    private void tick(DataScheduler scheduler) {
        if (getProperties().growStage >= getType().growMaxStage()) {
            return;
        }
        if (++getProperties().growStage == getType().growMaxStage()) {
            getProperties().harvesterID.get(getParent()).ifPresent(
                    h -> h.getWorkGroup().addWork(HarvestFromField.make(h, this))
            );
        }
        repair();
    }

    public interface Type<B extends FieldBuilding<?>> extends ConnectiveBuilding.Type<B>,
            BuildingFactory<B, GridCell, NearArgument<ConnectionArgument<BuildingLocation<GridCell>>>> {
        int growTickCost();
        int growMaxStage();
        int masterDistance();
        Map<Sellable, Integer> harvest();
        int harvestPhaseCost();
        Set<Block> harvestBlocks();

        default boolean harvesterSelector(HarvesterBuilding<?> building) {
            return true;
        }

        default boolean fieldConnector(Connection pointTo) {
            return pointTo.building().getType().getClass().equals(getClass());
        }

        @Override
        default GridsPattern<GridCell> getGridsPattern() {
            return GridCellPattern.of();
        }

        @Override
        default Stream<NearArgument<ConnectionArgument<BuildingLocation<GridCell>>>>
        filter(BuildingSet buildingSet, Stream<Grid> requiringGrids) {
            BuildingsFilter filter = new BuildingsFilter(buildingSet);
            return requiringGrids.flatMap(filter.withCell())
                    .map(filter.connectTo(GridSide.SOUTH, connectablePredicate(this::fieldConnector)))
                    .map(filter.near(masterDistance(), b -> b instanceof HarvesterBuilding<?> h && harvesterSelector(h)))
                    .filter(n -> !n.neighbors().isEmpty());
        }

        @Override
        default BuildingFactory<B, GridCell, NearArgument<ConnectionArgument<BuildingLocation<GridCell>>>> getFactory() {
            return this;
        }
    }

    public static class FieldProperty extends ConnectiveProperties {
        private Integer timerPhase;
        private int growStage;
        private ID<BuildingSet, HarvesterBuilding<?>> harvesterID;

        protected FieldProperty(
                int id,
                ID<BuildingSet, HarvesterBuilding<?>> harvesterID,
                BuildingLocation<GridCell> location,
                String name,
                String buildingPattern
        ) {
            super(id, location, name, buildingPattern);
            this.timerPhase = null;
            this.growStage = 0;
            this.harvesterID = harvesterID;
        }

        protected FieldProperty(
                Optional<Integer> timerPhase,
                int growStage,
                ID<BuildingSet, HarvesterBuilding<?>> harvesterID,
                ConnectiveProperties prop
        ) {
            super(prop);
            this.timerPhase = timerPhase.orElse(null);
            this.growStage = growStage;
            this.harvesterID = harvesterID;
        }

        public static Codec<FieldProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("timerPhase").forGetter(p -> Optional.ofNullable(p.timerPhase)),
                Codec.INT.fieldOf("growStage").forGetter(p -> p.growStage),
                ID.CODEC.fieldOf("harvesterID").forGetter(p -> p.harvesterID),
                ConnectiveProperties.CODEC.fieldOf("connectiveProperties").forGetter(p -> p)
        ).apply(instance, FieldProperty::new));
    }
}
