package com.outlook.hxsw.settlements.engine.data.id;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.buildings.BuildingSet;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.engine.data.TownSet;
import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import java.util.List;
import java.util.Optional;

public final class BuildingID<B extends Building> {
    public final ID<TownSet, Town> townID;
    public final ID<BuildingSet, B> buildingID;

    public BuildingID(ID<TownSet, Town> townID, ID<BuildingSet, B> buildingID) {
        this.townID = townID;
        this.buildingID = buildingID;
    }

    public static <B extends Building> BuildingID<B> empty() {
        return new BuildingID<>(ID.make(0), ID.make(0));
    }

    public static <B extends Building> BuildingID<B> of(B building) {
        Town town = building.getParent().getParent();
        return new BuildingID<>(ID.of(town), ID.of(building));
    }

    public Optional<B> get(SidecarData data) {
        return get(data.scheduler());
    }

    public Optional<B> get(DataScheduler scheduler) {
        return get(scheduler.data());
    }

    public Optional<B> get(SettlementsData data) {
        Optional<Town> town = townID.get(data.towns);
        if (town.isEmpty()) {
            return Optional.empty();
        }
        return buildingID.get(town.get().buildings);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BuildingID<?> b && b.townID.equals(townID) && b.buildingID.equals(buildingID);
    }

    public boolean isEmpty() {
        return townID.isEmpty() || buildingID.isEmpty();
    }

    @Override
    public int hashCode() {
        return 31 * townID.value + buildingID.value;
    }

    private BuildingID(List<Integer> list) {
        this(ID.make(list.getFirst()), ID.make(list.getLast()));
    }

    private List<Integer> intoIDs() {
        return List.of(townID.value, buildingID.value);
    }

    @SuppressWarnings("rawtypes")
    public static Codec<BuildingID> CODEC = Codec.list(Codec.INT, 2, 2).xmap(BuildingID::new, BuildingID::intoIDs);
}
