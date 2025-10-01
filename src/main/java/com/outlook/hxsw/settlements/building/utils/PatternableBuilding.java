package com.outlook.hxsw.settlements.building.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.buildings.BuildingLocation;
import com.outlook.hxsw.settlements.engine.grid.*;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.engine.schedule.ServerScheduler;
import com.outlook.hxsw.settlements.engine.schedule.Task;
import com.outlook.hxsw.settlements.engine.schedule.conditions.WhenChunkLoaded;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;


public abstract class PatternableBuilding<G extends Grids, P extends Enum<P>> extends Building {
    private final BasicProperties<G> properties;
    private P buildingPattern;

    protected PatternableBuilding(int id, P pattern, BuildingLocation<G> location, String name) {
        this(pattern.getDeclaringClass(), new BasicProperties<>(id, location, name, pattern.name()));
    }

    protected PatternableBuilding(Class<P> patternClass, BasicProperties<G> properties) {
        super(properties.id);
        this.properties = properties;
        this.buildingPattern = P.valueOf(patternClass, properties.buildingPattern);
    }

    protected abstract Consumer<ServerScheduler> getBuilder();

    public final void repair() {
        var scheduler = scheduler();
        if (scheduler == null) {
            return;
        }

        if (properties.repairingTask.getAndSet(getBuilder()) == null) {
            var trigger = new WhenChunkLoaded(getDimension(), StreamSupport.stream(getGrids().spliterator(), false).map(Grid::getChunk).distinct());
            scheduler.scheduleWhen(trigger, buildInServerSide(properties.repairingTask));
        }
    }

    private static Task<ServerScheduler, Void>
    buildInServerSide(AtomicReference<Consumer<ServerScheduler>> repairingTaskSlot) {
        return s -> {
            var task = repairingTaskSlot.getAndSet(null);
            if (task != null) {
                task.accept(s);
            }
            return null;
        };
    }

    @Override
    public final BuildingLocation<G> getLocation() {
        return properties.location;
    }

    public final P getBuildingPattern() {
        return buildingPattern;
    }

    public final void setBuildingPattern(P buildingPattern) {
        this.buildingPattern = buildingPattern;
        this.properties.buildingPattern = buildingPattern.name();
        repair();
    }

    protected BasicProperties<G> getProperties() {
        return properties;
    }

    @Override
    public final String getName() {
        return properties.name;
    }

    @Override
    public final void setName(String name) {
        properties.name = name;
    }

    @Override
    public void registerToDataScheduler(DataScheduler scheduler) {
        if (getProperties().needsRepairing) {
            repair();
        }
    }

    public static class BasicProperties<G extends Grids> {
        final int id;
        final BuildingLocation<G> location;
        String name;
        String buildingPattern;
        boolean needsRepairing;
        final AtomicReference<Consumer<ServerScheduler>> repairingTask = new AtomicReference<>();

        public BasicProperties(int id, BuildingLocation<G> location, String name, String buildingPattern, boolean needsRepairing) {
            this.id = id;
            this.location = location;
            this.name = name;
            this.buildingPattern = buildingPattern;
            this.needsRepairing = needsRepairing;
        }

        public BasicProperties(int id, BuildingLocation<G> location, String name, String buildingPattern) {
            this(id, location, name, buildingPattern, true);
        }

        protected BasicProperties(BasicProperties<G> prop) {
            this.id = prop.id;
            this.location = prop.location;
            this.name = prop.name;
            this.buildingPattern = prop.buildingPattern;
            this.needsRepairing = prop.needsRepairing;
        }

        public static <G extends Grids> Codec<BasicProperties<G>> codec(Codec<G> gCodec) {
            return RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("id").forGetter(p -> p.id),
                            BuildingLocation.codec(gCodec).fieldOf("location").forGetter(p -> p.location),
                            Codec.STRING.fieldOf("name").forGetter(p -> p.name),
                            Codec.STRING.fieldOf("variant").forGetter(p -> p.buildingPattern),
                            Codec.BOOL.fieldOf("needsRepairing").forGetter(p -> p.needsRepairing || p.repairingTask.get() != null)
                    ).apply(instance, BasicProperties::new)
            );
        }
    }
}
