package com.outlook.hxsw.settlements.engine.buildings;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.building.RegisteredBuildings;

import javax.annotation.Nullable;
import java.util.*;

public interface BuildingType<B extends Building> {
    String getName();
    String getDisplayKey();

    BuildingFactory<B, ?, ?> getFactory();

    Codec<B> codec();

    static @Nullable BuildingType<?> getType(String name) {
        return RegisteredBuildings.getType(name);
    }

    static Set<String> getTypes() {
        return RegisteredBuildings.getTypes();
    }
}
