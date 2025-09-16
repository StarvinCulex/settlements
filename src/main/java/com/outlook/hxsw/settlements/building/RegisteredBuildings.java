package com.outlook.hxsw.settlements.building;

import com.outlook.hxsw.settlements.building.agriculture.BuildingFarmField;
import com.outlook.hxsw.settlements.building.agriculture.BuildingFarmstead;
import com.outlook.hxsw.settlements.building.road.BuildingRoad;
import com.outlook.hxsw.settlements.engine.buildings.BuildingType;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class RegisteredBuildings {
    private RegisteredBuildings() {}

    /**
     * 调用此方法以加载所有建筑类型。
     * 新的建筑类型需要在这里注册。
     */
    public static void register() {
        register(BuildingRoad.Type.class);
        register(BuildingFarmstead.Type.class);
        register(BuildingFarmField.Type.class);
    }

    public static @Nullable BuildingType<?> getType(String name) {
        return registeredTypes.get(name);
    }

    public static Set<String> getTypes() {
        return Collections.unmodifiableSet(registeredTypes.keySet());
    }

    private static final Map<String, BuildingType<?>> registeredTypes = new HashMap<>();

    private static void register(BuildingType<?> type) {
        registeredTypes.put(type.getName(), type);
    }

    private static <T extends Enum<T> & BuildingType<?>> void register(Class<T> a) {
        for (var t : a.getEnumConstants()) {
            register(t);
        }
    }
}
