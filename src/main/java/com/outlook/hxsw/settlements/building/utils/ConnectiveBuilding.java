package com.outlook.hxsw.settlements.building.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.utils.filter.*;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.grid.*;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.*;

public abstract class ConnectiveBuilding<P extends Enum<P> & Supplier<ConnectiveBuildingPattern>> extends PatternableBuilding<GridCell, P> {
    private final int hCenter;

    protected ConnectiveBuilding(int id, P variant, ConnectionArgument<BuildingLocation<GridCell>> arg, String name) {
        this(
                variant.getDeclaringClass(),
                new ConnectiveProperties(id, arg.inner(), name, variant.name())
        );
        connectAll(arg.connections());
    }

    protected ConnectiveBuilding(Class<P> variantClass, ConnectiveProperties properties) {
        super(variantClass, properties);
        this.hCenter = 0;
    }

    public final Map<GridSide, Integer> getConnections() {
        return Collections.unmodifiableMap(getProperties().connections);
    }

    protected final void connectAll(Collection<Connection> collections) {
        for (var pointing : collections) {
            connectWithoutRepair(pointing.direction(), pointing.building());
        }
        repair();
    }

    protected final void connect(GridSide side, Building building) {
        connectWithoutRepair(side, building);
        repair();
    }


    protected final void connect(GridSide side, int deltaHeight) {
        connectWithoutRepair(side, deltaHeight);
        repair();
    }

    protected void disconnect(GridSide side) {
        getProperties().connections.remove(side);
        repair();
    }

    protected ConnectiveProperties getProperties() {
        return (ConnectiveProperties) super.getProperties();
    }

    protected PatternOption[] patternOptions() {
        return new PatternOption[]{};
    }

    @Override
    public Consumer<Scheduler.Server> getBuilder() {
        return getBuildingPattern().get().generateBuilder(getLocation(), getConnections(), patternOptions());
    }

    private void connectWithoutRepair(GridSide side, Building building) {
        int deltaHeight = building.getGroundY() - this.getGroundY();
        if (!(building instanceof ConnectiveBuilding<?> other)) {
            connect(side, deltaHeight);
            return;
        }

        int thisRange = getType().connectingHeightMax() - getType().connectingHeightMin();
        int otherRange = other.getType().connectingHeightMax() - other.getType().connectingHeightMin();
        int otherH = (thisRange * other.hCenter + otherRange * this.hCenter - deltaHeight * otherRange)
                / (thisRange + otherRange);
        int thisH = deltaHeight + otherH;
        connectWithoutRepair(side, thisH);
        other.connect(side.getClockWise(), otherH);
    }

    private void connectWithoutRepair(GridSide side, int deltaHeight) {
        if (getType().connectingHeightMax() < deltaHeight || deltaHeight < getType().connectingHeightMin()) {
            System.out.printf("[settlements] failed in connecting %s side of %s with deltaHeight %d.", side, this, deltaHeight);
            return;
        }
        getProperties().connections.put(side, deltaHeight);
    }

    @Override
    public abstract Type<?> getType();

    public interface Type<B extends ConnectiveBuilding<?>> extends BuildingType<B> {
        int connectingHeightMin();
        int connectingHeightMax();

        default BiPredicate<BuildingLocation<GridCell>, Connection> connectablePredicate(Predicate<Connection> filter) {
            return (at, pointTo) -> canConnectTo(at.groundY(), pointTo.building()) && filter.test(pointTo);
        }

        default boolean canConnectTo(int groundY, Building other) {
            int hMin = connectingHeightMin();
            int hMax = connectingHeightMax();
            if (other instanceof ConnectiveBuilding<?> c) {
                hMin -= c.getType().connectingHeightMax();
                hMax -= c.getType().connectingHeightMin();
            }
            int deltaHeight = other.getGroundY() - groundY;
            return hMin <= deltaHeight && deltaHeight <= hMax;
        }
    }

    public static class ConnectiveProperties extends BasicProperties<GridCell> {
        Map<GridSide, Integer> connections;

        private ConnectiveProperties(BasicProperties<GridCell> basic, Map<GridSide, Integer> connections) {
            super(basic);
            if (connections.isEmpty()) {
                this.connections = new EnumMap<>(GridSide.class);
            } else {
                this.connections = new EnumMap<>(connections);
            }
        }

        protected ConnectiveProperties(ConnectiveProperties prop) {
            this(prop, prop.connections);
        }

        public ConnectiveProperties(
                int id,
                BuildingLocation<GridCell> location,
                String name,
                String buildingPattern
        ) {
            super(id, location, name, buildingPattern);
            this.connections = new EnumMap<>(GridSide.class);
        }

        public static Codec<ConnectiveProperties> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BasicProperties.codec(GridCell.CODEC).fieldOf("basic").forGetter(p -> p),
                        Codec.simpleMap(GridSide.CODEC, Codec.INT, GridSide.KEYABLE).fieldOf("connections").forGetter(p -> p.connections)
                ).apply(instance, ConnectiveProperties::new)
        );
    }
}
