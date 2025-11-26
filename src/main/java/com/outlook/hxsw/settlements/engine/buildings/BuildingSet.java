package com.outlook.hxsw.settlements.engine.buildings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.*;
import com.outlook.hxsw.settlements.engine.data.utils.ChildContainer;
import com.outlook.hxsw.settlements.engine.grid.*;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.engine.schedule.ServerScheduler;
import com.outlook.hxsw.settlements.engine.schedule.VoidTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public final class BuildingSet extends ChildContainer<Building, Town> {
    final Map<Grid, Building> gridMap = new HashMap<>();
    Map<Grid, GridTerrain> terrainMap;

    public BuildingSet() {
        super(Town.class);
        this.terrainMap = new HashMap<>();
    }

    public BuildingSet(int nextBuildingID, Collection<Building> buildings, Map<Grid, GridTerrain> terrainMap) {
        super(Town.class, nextBuildingID, buildings);
        buildings.forEach(this::paintIntoGridMap);
        this.terrainMap = terrainMap;
    }

    public Map<Grid, GridTerrain> getTerrainMap() {
        return Collections.unmodifiableMap(terrainMap);
    }

    public Map<Grid, Building> getGridMap() {
        return Collections.unmodifiableMap(gridMap);
    }

    public <B extends Building, G extends Grids, X extends BuildingArgument>
    Optional<B> makeAndPlace(BuildingFactory<B, G, X> factory, Grid placingAt) {
        return factory.filter(this, Stream.of(placingAt))
                .findFirst()
                .map(x -> {
                    B b = factory.make(generateChildID(), x);
                    add(b);
                    return b;
                });
    }

    @Override
    public void registerToDataScheduler(DataScheduler scheduler) {
        super.registerToDataScheduler(scheduler);
        if (terrainMap.isEmpty()) {
            System.out.println("BuildingSet of " + getParent() + " is empty. Make a TerrainSurveyor to survey.");
            scheduler.schedule(new TerrainSurveyor(this.getParent()));
        }
    }

    public static final Codec<BuildingSet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("nextBuildingID").forGetter(BuildingSet::getNextChildID),
                    Codec.list(Building.CODEC).fieldOf("buildings")
                            .forGetter(BuildingSet::getChildrenList),
                    Codec.unboundedMap(Grid.CODEC, GridTerrain.CODEC).fieldOf("terrainMap")
                            .forGetter(BuildingSet::getTerrainMap)
            ).apply(instance, BuildingSet::new)
    );

    @Override
    protected void add(Building building) throws GridOccupiedException {
        paintIntoGridMap(building);
        super.add(building);
    }

    private void paintIntoGridMap(Building building) throws GridOccupiedException {
        Grids grids = building.getGrids();
        for (Grid g : grids) {
            if (gridMap.containsKey(g)) {
                throw new GridOccupiedException(g, building, gridMap.get(g));
            }
        }
        for (Grid g : grids) {
            gridMap.put(g, building);
        }
    }

    void setTerrainMap(Map<Grid, GridTerrain> terrainMap) {
        this.terrainMap = terrainMap;
    }
}

class TerrainSurveyor implements VoidTask<ServerScheduler> {
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
    public void run(ServerScheduler scheduler) throws RuntimeException {
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
        scheduler.schedule(this::callback);
    }

    private void callback(DataScheduler scheduler) {
        consumer.apply(scheduler.data()).accept(destTerrainMap);
    }
}