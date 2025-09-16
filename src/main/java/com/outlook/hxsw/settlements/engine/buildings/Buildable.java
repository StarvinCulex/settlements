package com.outlook.hxsw.settlements.engine.buildings;


import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;
import com.outlook.hxsw.settlements.utils.grid.Grids;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface Buildable extends SidecarData {
    /**
     * 建筑实例关联的BuildingType是可以动态改变的。
     * @return 此建筑关联的BuildingType。返回类型的泛型参数必须是此建筑类型。
     */
    UUID getUUID();
    BuildingType<?> getType();
    String getName();
    void setName(String name);

    BuildingLocation<?> getLocation();

    default ResourceKey<Level> getDimension() {
        return getLocation().dimension();
    }

    default int getGroundY() {
        return getLocation().groundY();
    }

    default Grids getGrids() {
        return getLocation().grids();
    }

    Codec<Buildable> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Buildable, T>> decode(DynamicOps<T> ops, T input) {
            // 1. 先解析 type 字段
            DataResult<T> typeField = ops.get(input, "type");
            return typeField.flatMap(typeVal -> {
                DataResult<String> typeString = Codec.STRING.decode(ops, typeVal).map(Pair::getFirst);
                return typeString.flatMap(typeName -> {
                    BuildingType<?> type = BuildingType.getType(typeName);
                    if (type == null) {
                        // 明确指定类型，避免通配 capture 报错
                        return DataResult.<Pair<Buildable, T>>error(() -> "Unknown buildable type: " + typeName);
                    }
                    // 由于 type.codec().decode 返回的是 Pair<? extends Buildable, T>
                    // 强转一下
                    @SuppressWarnings("unchecked")
                    DataResult<Pair<Buildable, T>> res = (DataResult<Pair<Buildable, T>>) (DataResult<?>) type.codec().decode(ops, input);
                    return res;
                });
            });
        }

        @Override
        public <T> DataResult<T> encode(Buildable input, DynamicOps<T> ops, T prefix) {
            String typeName = input.getType().getName();
            T typeValue = ops.createString(typeName);

            @SuppressWarnings("unchecked")
            Codec<Buildable> codec = (Codec<Buildable>) input.getType().codec();
            DataResult<T> innerResult = codec.encode(input, ops, prefix);

            return innerResult.flatMap(innerMap -> {
                var builder = ops.mapBuilder();
                builder.add("type", typeValue);
                var mapRes = ops.getMap(innerMap);
                if (mapRes.error().isPresent()) {
                    return DataResult.error(() -> "Buildable innerCodec result not a map: type=" + typeName);
                }
                mapRes.result().get().entries().forEach(e -> builder.add(e.getFirst(), e.getSecond()));
                return builder.build(prefix);
            });
        }
    };
}
