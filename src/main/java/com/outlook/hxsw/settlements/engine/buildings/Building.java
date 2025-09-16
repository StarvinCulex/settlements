package com.outlook.hxsw.settlements.engine.buildings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.WithParent;
import com.outlook.hxsw.settlements.utils.grid.*;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;
import com.outlook.hxsw.settlements.engine.schedule.ServerTask;
import com.outlook.hxsw.settlements.engine.schedule.tools.ServerTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;


public abstract class Building<G extends Grids, P extends Enum<P>>
        extends WithParent<BuildingSet> implements Buildable
{
    private final BasicProperties<G> properties;
    private P buildingPattern;

    protected Building(P pattern, BuildingLocation<G> location, String name) {
        this(pattern.getDeclaringClass(), new BasicProperties<>(location, name, pattern.name()));
    }

    protected Building(Class<P> patternClass, BasicProperties<G> properties) {
        this.properties = properties;
        this.buildingPattern = P.valueOf(patternClass, properties.buildingPattern);
    }

    protected abstract Consumer<Scheduler.Server> getBuilder();

    public final void repair() {
        var scheduler = scheduler();
        if (scheduler == null) {
            return;
        }

        if (properties.repairingTask.getAndSet(getBuilder()) == null) {
            var trigger = new ServerTrigger.WhenChunkLoaded(getDimension(), StreamSupport.stream(getGrids().spliterator(), false).map(Grid::getChunk).distinct());
            scheduler.register(new ServerTask(trigger, buildInServerSide(properties.repairingTask)));
        }
    }

    private static Consumer<Scheduler.Server>
    buildInServerSide(AtomicReference<Consumer<Scheduler.Server>> repairingTaskSlot) {
        return s -> {
            var task = repairingTaskSlot.getAndSet(null);
            if (task != null) {
                task.accept(s);
            }
        };
    }

    @Override
    public final UUID getUUID() {
        return properties.uuid;
    }

    @Override
    public final ResourceKey<Level> getDimension() {
        return getLocation().dimension();
    }

    @Override
    public final int getGroundY() {
        return getLocation().groundY();
    }

    @Override
    public final Grids getGrids() {
        return getLocation().grids();
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
    public void registerToSidecar(Scheduler.Sidecar scheduler) {
        if (getProperties().needsRepairing) {
            repair();
        }
    }

    @Override
    public String toString() {
        return String.format("%s<%s>", getType().getName(), getGrids().begin());
    }

    public static class BasicProperties<G extends Grids> {
        final UUID uuid;
        final BuildingLocation<G> location;
        String name;
        String buildingPattern;
        boolean needsRepairing;
        final AtomicReference<Consumer<Scheduler.Server>> repairingTask = new AtomicReference<>();

        public BasicProperties(UUID uuid, BuildingLocation<G> location, String name, String buildingPattern, boolean needsRepairing) {
            this.uuid = uuid;
            this.location = location;
            this.name = name;
            this.buildingPattern = buildingPattern;
            this.needsRepairing = needsRepairing;
        }

        public BasicProperties(BuildingLocation<G> location, String name, String buildingPattern) {
            this(UUID.randomUUID(), location, name, buildingPattern, true);
        }

        protected BasicProperties(BasicProperties<G> prop) {
            this.uuid = prop.uuid;
            this.location = prop.location;
            this.name = prop.name;
            this.buildingPattern = prop.buildingPattern;
            this.needsRepairing = prop.needsRepairing;
        }

        public static <G extends Grids> Codec<BasicProperties<G>> codec(Codec<G> gCodec) {
            return RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(p -> p.uuid),
                            BuildingLocation.codec(gCodec).fieldOf("location").forGetter(p -> p.location),
                            Codec.STRING.fieldOf("name").forGetter(p -> p.name),
                            Codec.STRING.fieldOf("variant").forGetter(p -> p.buildingPattern),
                            Codec.BOOL.fieldOf("needsRepairing").forGetter(p -> p.needsRepairing || p.repairingTask.get() != null)
                    ).apply(instance, BasicProperties::new)
            );
        }
    }
}
