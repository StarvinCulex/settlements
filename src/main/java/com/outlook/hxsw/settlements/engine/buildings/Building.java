package com.outlook.hxsw.settlements.engine.buildings;


import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.building.RegisteredBuildings;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecElement;
import com.outlook.hxsw.settlements.engine.data.utils.WithParentAndID;
import com.outlook.hxsw.settlements.engine.folks.pos.InBuilding;
import com.outlook.hxsw.settlements.engine.grid.Grids;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;
import java.util.function.Function;

public abstract class Building extends WithParentAndID<BuildingSet> implements CodecElement {
    public Building(int id) {
        super(BuildingSet.class, id);
    }

    /**
     * 建筑实例关联的BuildingType是可以动态改变的。
     * @return 此建筑关联的BuildingType。返回类型的泛型参数必须是此建筑类型。
     */
    public abstract BuildingType<?> getType();
    public abstract String getName();
    public abstract void setName(String name);

    public abstract BuildingLocation<?> getLocation();

    public final ResourceKey<Level> getDimension() {
        return getLocation().dimension();
    }

    public final int getGroundY() {
        return getLocation().groundY();
    }

    public int getFootingY() {
        return getGroundY();
    }

    private static final Set<Block> DEFAULT_PLACE_ON = Set.of(Blocks.DIRT_PATH, Blocks.GRASS_BLOCK);
    public InBuilding getFolkPos() {
        return new InBuilding(this, DEFAULT_PLACE_ON);
    }

    public final Grids getGrids() {
        return getLocation().grids();
    }

    @Override
    public String toString() {
        return getType().getName() + toShortString();
    }

    public String toShortString() {
        return String.format("#%d<%s>", id, getGrids().center());
    }

    public static final Codec<Building> CODEC = RegisteredBuildings.INSTANCE;
}
