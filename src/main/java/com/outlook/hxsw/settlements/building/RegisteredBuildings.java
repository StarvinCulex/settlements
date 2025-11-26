package com.outlook.hxsw.settlements.building;

import com.outlook.hxsw.settlements.building.agriculture.BuildingFarmField;
import com.outlook.hxsw.settlements.building.agriculture.BuildingFarmstead;
import com.outlook.hxsw.settlements.building.road.BuildingRoad;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.buildings.BuildingType;
import com.outlook.hxsw.settlements.engine.data.codecs.DynamicCodecer;

public final class RegisteredBuildings extends DynamicCodecer<Building, BuildingType<?>> {
    public static final RegisteredBuildings INSTANCE = new RegisteredBuildings();

    /**
     * 调用此方法以加载所有建筑类型。
     * 新的建筑类型需要在这里注册。
     */
    private RegisteredBuildings() {
        register(BuildingRoad.Type.class);
        register(BuildingFarmstead.Type.class);
        register(BuildingFarmField.Type.class);
    }

    private <T extends Enum<T> & BuildingType<?>> void register(Class<T> a) {
        for (var t : a.getEnumConstants()) {
            register(t);
        }
    }
}
