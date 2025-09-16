package com.outlook.hxsw.settlements.engine.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.buildings.BuildingSet;
import com.outlook.hxsw.settlements.engine.data.utils.WithParent;
import com.outlook.hxsw.settlements.utils.grid.*;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Objects;
import java.util.function.Function;

public class Town extends WithParent<SettlementsData> {
    private final GridRegion border;
    private final int id;
    private String name;
    private final BuildingSet buildingSet;
    private final ResourceKey<Level> dimension;

    public Town(
            int id,
            String name,
            GridRegion border,
            BuildingSet buildings,
            ResourceKey<Level> dimension
    ) {
        this.id = id;
        this.name = name;
        this.border = border;
        this.buildingSet = buildings;
        this.dimension = Objects.requireNonNull(dimension);

        this.buildingSet.setParent(this);
    }

    public Town(int id, String name, Vec3i center, ResourceKey<Level> dimension) {
        this(id, name, generateBorder(center), new BuildingSet(), dimension);
    }

    public int getID() {
        return id;
    }

    public String getName()  {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GridRegion getBorder() {
        return border;
    }

    public BuildingSet buildings() {
        return buildingSet;
    }

    public ResourceKey<Level> getDimensionKey() {
        return dimension;
    }

    public Function<SettlementsData, Town> getReference() {
        int id = this.id;
        return (SettlementsData data) -> data.getTown(id);
    }

    private static final int RADIUS = 10;
    private static GridRegion generateBorder(Vec3i center) {
        return new GridRegion(new Grid(center), RADIUS);
    }

    public static final Codec<Town> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("id").forGetter(Town::getID),
                    Codec.STRING.fieldOf("name").forGetter(Town::getName),
                    GridRegion.CODEC.fieldOf("border").forGetter(Town::getBorder),
                    BuildingSet.CODEC.fieldOf("buildings").forGetter(Town::buildings),
                    Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Town::getDimensionKey)
            ).apply(instance, Town::new)
    );

    @Override
    public void registerToSidecar(Scheduler.Sidecar sidecar) {
        this.buildingSet.registerToSidecar(sidecar);
    }


    public static class OverlapException extends RuntimeException {
        private final Town conflictTown;

        public OverlapException(Town conflictTown) {
            super("town overlap " + conflictTown.getName());
            this.conflictTown = conflictTown;
        }

        public Town getConflictTown() {
            return conflictTown;
        }
    }
}
