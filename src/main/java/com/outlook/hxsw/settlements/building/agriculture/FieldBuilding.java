package com.outlook.hxsw.settlements.building.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.utils.*;
import com.outlook.hxsw.settlements.building.utils.filter.*;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;
import com.outlook.hxsw.settlements.engine.schedule.SidecarTimer;
import com.outlook.hxsw.settlements.utils.grid.*;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class FieldBuilding<P extends Enum<P> & Supplier<ConnectiveBuildingPattern>> extends ConnectiveBuilding<P> {
    private @Nullable SidecarTimer timer = null;

    protected FieldBuilding(
            P variant,
            NearArgument<ConnectionArgument<BuildingLocation<GridCell>>> arg,
            String name
    ) {
        super(variant.getDeclaringClass(), new FieldProperty(
                arg.neighbors().stream().findFirst().orElseThrow().getUUID(),
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
        var b = getParent().get(getProperties().harvesterUUID);
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
    public void registerToSidecar(Scheduler.Sidecar scheduler) {
        super.registerToSidecar(scheduler);
        if (timer == null) {
            if (getProperties().timerPhase == null) {
                getProperties().timerPhase = scheduler.getPhase();
            }
            timer = new SidecarTimer(getProperties().timerPhase, getType().growTickCost(), this::tick);
        }
        scheduler.register(timer);
    }

    private void tick(Scheduler.Sidecar scheduler) {
        if (getProperties().growStage >= getType().growMaxStage()) {
            return;
        }
        if (++getProperties().growStage == getType().growMaxStage()) {
            getHarvester().ifPresent(Harvester::harvest);
        }
        repair();
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
        private UUID harvesterUUID;

        protected FieldProperty(
                UUID harvesterUUID,
                BuildingLocation<GridCell> location,
                String name,
                String buildingPattern
        ) {
            super(location, name, buildingPattern);
            this.timerPhase = null;
            this.growStage = 0;
            this.harvesterUUID = harvesterUUID;
        }

        protected FieldProperty(Optional<Integer> timerPhase, int growStage, UUID harvesterUUID, ConnectiveProperties prop) {
            super(prop);
            this.timerPhase = timerPhase.orElse(null);
            this.growStage = growStage;
            this.harvesterUUID = harvesterUUID;
        }

        public static Codec<FieldProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("timerPhase").forGetter(p -> Optional.ofNullable(p.timerPhase)),
                Codec.INT.fieldOf("growStage").forGetter(p -> p.growStage),
                Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("harvesterUUID").forGetter(p -> p.harvesterUUID),
                ConnectiveProperties.CODEC.fieldOf("connectiveProperties").forGetter(p -> p)
        ).apply(instance, FieldProperty::new));
    }
}
