package com.outlook.hxsw.settlements.engine.folks.pos;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.data.id.BuildingID;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class InBuilding implements FolkPos {
    public final BuildingID<Building> buildingID;
    private final ResourceKey<Level> dimension;
    private final Grid grid;

    public InBuilding(Building building) {
        this.buildingID = BuildingID.of(building);
        this.dimension = building.getDimension();
        this.grid = building.getGrids().center();
    }

    @Override
    public ResourceKey<Level> dimension() {
        return dimension;
    }

    @Override
    public Grid grid() {
        return grid;
    }

    @Override
    public CodecType<?> getType() {
        return Type.INSTANCE;
    }

    private InBuilding(BuildingID<Building> buildingID, ResourceKey<Level> dimension, Grid grid) {
        this.buildingID = buildingID;
        this.dimension = dimension;
        this.grid = grid;
    }

    public static final Codec<InBuilding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuildingID.CODEC.fieldOf("buildingID").forGetter(b -> b.buildingID),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(InBuilding::dimension),
            Grid.CODEC.fieldOf("grid").forGetter(InBuilding::grid)
    ).apply(instance, InBuilding::new));

    static final class Type implements CodecType<InBuilding> {
        static final Type INSTANCE = new Type();

        @Override
        public String getName() {
            return "InBuilding";
        }

        @Override
        public Codec<InBuilding> codec() {
            return CODEC;
        }
    }
}
