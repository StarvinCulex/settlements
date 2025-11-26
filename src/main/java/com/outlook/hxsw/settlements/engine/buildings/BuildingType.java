package com.outlook.hxsw.settlements.engine.buildings;

import com.outlook.hxsw.settlements.building.RegisteredBuildings;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;

import javax.annotation.Nullable;
import java.util.*;

public interface BuildingType<B extends Building> extends CodecType<B> {
    String getDisplayKey();

    BuildingFactory<B, ?, ?> getFactory();

    static @Nullable BuildingType<?> getType(String name) {
        return RegisteredBuildings.INSTANCE.getType(name);
    }

    static Set<String> getTypes() {
        return RegisteredBuildings.INSTANCE.getTypes();
    }
}
