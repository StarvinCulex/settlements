package com.outlook.hxsw.settlements.engine.buildings;


import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.outlook.hxsw.settlements.engine.data.utils.WithParentAndID;
import com.outlook.hxsw.settlements.engine.grid.Grids;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class Building extends WithParentAndID<BuildingSet> {
    public Building(int id) {
        super(id);
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

    public static final Codec<Building> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Building, T>> decode(DynamicOps<T> ops, T input) {
            // 1. 先解析 type 字段
            DataResult<T> typeField = ops.get(input, "type");
            return typeField.flatMap(typeVal -> {
                DataResult<String> typeString = Codec.STRING.decode(ops, typeVal).map(Pair::getFirst);
                return typeString.flatMap(typeName -> {
                    BuildingType<?> type = BuildingType.getType(typeName);
                    if (type == null) {
                        // 明确指定类型，避免通配 capture 报错
                        return DataResult.<Pair<Building, T>>error(() -> "Unknown buildable type: " + typeName);
                    }
                    // 由于 type.codec().decode 返回的是 Pair<? extends Buildable, T>
                    // 强转一下
                    @SuppressWarnings("unchecked")
                    DataResult<Pair<Building, T>> res = (DataResult<Pair<Building, T>>) (DataResult<?>) type.codec().decode(ops, input);
                    return res;
                });
            });
        }

        @Override
        public <T> DataResult<T> encode(Building input, DynamicOps<T> ops, T prefix) {
            String typeName = input.getType().getName();
            T typeValue = ops.createString(typeName);

            @SuppressWarnings("unchecked")
            Codec<Building> codec = (Codec<Building>) input.getType().codec();
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
