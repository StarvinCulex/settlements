package com.outlook.hxsw.settlements.building.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.utils.*;
import com.outlook.hxsw.settlements.building.utils.filter.*;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.engine.grid.*;
import com.outlook.hxsw.settlements.engine.schedule.ScheduledTask;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class FieldBuilding<P extends Enum<P> & Supplier<ConnectiveBuildingPattern>> extends ConnectiveBuilding<P> {
    private @Nullable ScheduledTask<Void, ?> timer = null;

    protected FieldBuilding(
            int id,
            P variant,
            NearArgument<ConnectionArgument<BuildingLocation<GridCell>>> arg,
            String name
    ) {
        super(variant.getDeclaringClass(), new FieldProperty(
                id,
                arg.neighbors().stream().findFirst().orElseThrow().getID(),
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

    public Optional<Harvester> getHarvester() {
        var b = getParent().get(getProperties().harvesterID);
        if (b.isPresent() && b.get() instanceof Harvester h) {
            return Optional.of(h);
        }
        return Optional.empty();
    }

    @Override
    public abstract Type<? extends FieldBuilding<P>> getType();

    @Override
    public FieldProperty getProperties() {
        return (FieldProperty) super.getProperties();
    }

    @Override
    public void registerToDataScheduler(DataScheduler scheduler) {
        super.registerToDataScheduler(scheduler);
        if (timer == null) {
            if (getProperties().timerPhase == null) {
                getProperties().timerPhase = scheduler.getPhase();
            }
            timer = scheduler.runPeriodically(getProperties().timerPhase, getType().growTickCost(), this::tick);
        }
    }

    private Void tick(DataScheduler scheduler) {
        if (getProperties().growStage >= getType().growMaxStage()) {
            return null;
        }
        if (++getProperties().growStage == getType().growMaxStage()) {
            getHarvester().ifPresent(Harvester::harvest);
        }
        repair();
        return null;
    }

    public interface Type<B extends FieldBuilding<?>> extends ConnectiveBuilding.Type<B>,
            BuildingFactory<B, GridCell, NearArgument<ConnectionArgument<BuildingLocation<GridCell>>>> {
        int growTickCost();
        int growMaxStage();
        int masterDistance();

        default boolean harvesterSelector(Harvester building) {
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
                    .map(filter.near(masterDistance(), b -> b instanceof Harvester h && harvesterSelector(h)))
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
        private int harvesterID;

        protected FieldProperty(
                int id,
                int harvesterID,
                BuildingLocation<GridCell> location,
                String name,
                String buildingPattern
        ) {
            super(id, location, name, buildingPattern);
            this.timerPhase = null;
            this.growStage = 0;
            this.harvesterID = harvesterID;
        }

        protected FieldProperty(Optional<Integer> timerPhase, int growStage, int harvesterID, ConnectiveProperties prop) {
            super(prop);
            this.timerPhase = timerPhase.orElse(null);
            this.growStage = growStage;
            this.harvesterID = harvesterID;
        }

        public static Codec<FieldProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("timerPhase").forGetter(p -> Optional.ofNullable(p.timerPhase)),
                Codec.INT.fieldOf("growStage").forGetter(p -> p.growStage),
                Codec.INT.fieldOf("harvesterID").forGetter(p -> p.harvesterID),
                ConnectiveProperties.CODEC.fieldOf("connectiveProperties").forGetter(p -> p)
        ).apply(instance, FieldProperty::new));
    }
}
