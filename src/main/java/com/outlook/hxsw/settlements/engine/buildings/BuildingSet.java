package com.outlook.hxsw.settlements.engine.buildings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.*;
import com.outlook.hxsw.settlements.engine.data.utils.WithParent;
import com.outlook.hxsw.settlements.utils.grid.*;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;
import com.outlook.hxsw.settlements.engine.schedule.ServerTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public final class BuildingSet extends WithParent<Town> {
    Map<Grid, GridTerrain> terrainMap;
    final Map<Grid, Buildable> gridMap;
    final Map<UUID, Buildable> buildings;

    public BuildingSet() {
        this.terrainMap = new HashMap<>();
        this.gridMap = new HashMap<>();
        this.buildings = new HashMap<>();
    }

    public BuildingSet(Map<Grid, GridTerrain> terrainMap, List<? extends Buildable> buildings) {
        this();
        addAll(buildings);
    }

    public Map<Grid, GridTerrain> getTerrainMap() {
        return Collections.unmodifiableMap(terrainMap);
    }

    public Map<Grid, Buildable> getGridMap() {
        return Collections.unmodifiableMap(gridMap);
    }

    public Collection<Buildable> getBuildings() {
        return Collections.unmodifiableCollection(buildings.values());
    }

    public <B extends Buildable, G extends Grids, X extends BuildingArgument>
    Optional<B> makeAndPlace(BuildingFactory<B, G, X> factory, Grid placingAt) {
        return factory.filter(this, Stream.of(placingAt))
                .findFirst()
                .map(x -> {
                    B b = factory.make(x);
                    add(b);

                    return b;
                });
    }

    public Optional<Buildable> get(UUID uuid) {
        return Optional.ofNullable(buildings.get(uuid));
    }

    @Override
    public void registerToSidecar(Scheduler.Sidecar sidecar) {
        System.out.println("BuildingSet of " + getParent() + " is empty. Make a TerrainSurveyor to survey.");
        scheduler().run(new ServerTask(x -> true, new TerrainSurveyor(this.getParent())));
        buildings.values().forEach(b -> b.registerToSidecar(sidecar));
    }

    public static final Codec<BuildingSet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Grid.CODEC, GridTerrain.CODEC).fieldOf("terrainMap")
                            .forGetter(BuildingSet::getTerrainMap),
                    Codec.list(Buildable.CODEC).fieldOf("buildings")
                            .forGetter(bs -> bs.buildings.values().stream().toList())
            ).apply(instance, BuildingSet::new)
    );

    void add(Buildable building) throws GridOccupiedException {
        if (buildings.putIfAbsent(building.getUUID(), building) != null) {
            return;
        }

        Grids grids = building.getGrids();
        for (Grid g : grids) {
            if (gridMap.containsKey(g)) {
                throw new GridOccupiedException(g, building, gridMap.get(g));
            }
        }
        for (Grid g : grids) {
            gridMap.put(g, building);
        }

        if (building instanceof Building<?, ?> b) {
            b.setParent(this);
        }
    }

    void addAll(Collection<? extends Buildable> buildings) throws GridOccupiedException {
        buildings.forEach(this::add);
    }

    void setTerrainMap(Map<Grid, GridTerrain> terrainMap) {
        this.terrainMap = terrainMap;
    }
}

class TerrainSurveyor implements Consumer<Scheduler.Server> {
    private final Function<SettlementsData, Consumer<Map<Grid, GridTerrain>>> consumer;
    private final Grids grids;
    private final ResourceKey<Level> dimension;
    private final Map<Grid, GridTerrain> destTerrainMap = new HashMap<>();

    TerrainSurveyor(Town town) {
        this.consumer = town.getReference().andThen(t -> t.buildings()::setTerrainMap);
        this.dimension = town.getDimensionKey();
        this.grids = town.getBorder();
    }

    @Override
    public void accept(Scheduler.Server scheduler) throws RuntimeException {
        ServerLevel level = scheduler.server().getLevel(dimension);
        if (level != null) {
            for (Grid g : grids) {
                var surveyor = new GridTerrain.Surveyor(g);
                GridTerrain terrain = surveyor.survey(level);
                destTerrainMap.put(g, terrain);
            }
            System.out.println("[settlements] surveyed " + grids.stream().count() + " grids");
        } else {
            System.out.println("[settlements] cannot survey because level is null");
        }
        scheduler.run(this::callback);
    }

    private void callback(Scheduler.Sidecar sidecar) {
        consumer.apply(sidecar.data()).accept(destTerrainMap);
    }
}