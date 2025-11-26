package com.outlook.hxsw.settlements.engine.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.ChildContainer;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class TownSet extends ChildContainer<Town, SettlementsData> {
    public TownSet() {
        super(SettlementsData.class);
    }

    public TownSet(int nextChildID, List<Town> children) {
        super(SettlementsData.class);
        addAll(children);
    }

    public Optional<Town> get(ResourceKey<Level> dimension, Grid grid) {
        return getChildren().values().stream().filter(t -> t.getBorder().contains(grid)).findFirst();
    }

    public Town makeChild(String name, Vec3i center, ResourceKey<Level> dimension) throws Town.OverlapException {
        Town town = new Town(generateChildID(), name, center, dimension);
        add(town);
        return town;
    }

    @Override
    public void add(Town town) throws Town.OverlapException {
        getChildren().values().stream()
                .filter(t -> t.getDimensionKey().equals(town.getDimensionKey()) && t.getBorder().intersects(town.getBorder()))
                .findFirst()
                .ifPresent(t -> {throw new Town.OverlapException(t);});
        super.add(town);
    }

    public static Codec<TownSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("nextChildID").forGetter(TownSet::getNextChildID),
            Codec.list(Town.CODEC).fieldOf("children").forGetter(TownSet::getChildrenList)
    ).apply(instance, TownSet::new));
}
