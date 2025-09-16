package com.outlook.hxsw.settlements.engine.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;
import com.outlook.hxsw.settlements.utils.grid.Grid;
import com.outlook.hxsw.settlements.utils.grid.Grids;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;

public final class SettlementsData implements SidecarData {
    private Scheduler.Sidecar sidecar;

    private int nextTownID;
    private final Map<Integer, Town> towns;

    SettlementsData(int nextTownID, List<Town> townList) {
        this.nextTownID = nextTownID;
        this.towns = new HashMap<>();
        townList.forEach(this::addTown);
    }

    SettlementsData() {
        this(1, List.of());
    }

    @Override
    public void registerToSidecar(Scheduler.Sidecar sidecar) {
        this.sidecar = sidecar;
        towns.values().forEach(t -> t.registerToSidecar(sidecar));
    }

    @Override
    public Scheduler.Sidecar scheduler() {
        return sidecar;
    }

    public Map<Integer, Town> getTowns() {
        return Collections.unmodifiableMap(towns);
    }

    public int generateTownID() {
        return nextTownID++;
    }

    public @Nullable Town getTown(int id) {
        return towns.get(id);
    }

    public Optional<Town> getTown(ResourceKey<Level> dimension, Grid grid) {
        return towns.values().stream().filter(t -> t.getBorder().contains(grid)).findFirst();
    }

    public Iterable<Town> getTowns(Grids grids) {
        return towns.values().stream()
                .filter(t -> t.getBorder().intersects(grids))
                .toList();
    }

    public void addTown(Town town) throws Town.OverlapException {
        towns.values().stream()
                .filter(t -> t.getDimensionKey().equals(town.getDimensionKey()) && t.getBorder().intersects(town.getBorder()))
                .findFirst()
                .ifPresent(t -> {throw new Town.OverlapException(t);});

        boolean success = towns.putIfAbsent(town.getID(), town) == null;
        if (!success) {
            throw new RuntimeException("key %d already exists!".formatted(town.getID()));
        }

        town.setParent(this);
    }

    public static final Codec<SettlementsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("next_town_id").forGetter(d -> d.nextTownID),
                    Codec.list(Town.CODEC).fieldOf("towns").forGetter(d -> d.getTowns().values().stream().toList())
            ).apply(instance, SettlementsData::new)
    );
}
