package com.outlook.hxsw.settlements.engine.data.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class DynamicCodecer<E extends CodecElement, T extends CodecType<? extends E>> implements Codec<E> {
    private final Map<String, T> registeredTypes = new HashMap<>();
    @SafeVarargs
    protected DynamicCodecer(T... ts) {
        for (T t : ts) {
            register(t);
        }
    }

    protected void register(T t) {
        registeredTypes.put(t.getName(), t);
    }

    public final Set<String> getTypes() {
        return Collections.unmodifiableSet(registeredTypes.keySet());
    }

    public final @Nullable T getType(String name) {
        return registeredTypes.get(name);
    }

    public final <U> DataResult<Pair<E, U>> decode(DynamicOps<U> ops, U input) {
        try {
            U typeInput = ops.get(input, "type").getOrThrow(DecodeException::new);
            String typeName = Codec.STRING.decode(ops, typeInput).getOrThrow(DecodeException::new).getFirst();
            T type = getType(typeName);
            if (type == null) {
                return DataResult.error(() -> "unknown type: " + typeName);
            }

            @SuppressWarnings("unchecked")
            DataResult<Pair<E, U>> res = (DataResult<Pair<E, U>>) (DataResult<?>) type.codec().decode(ops, input);
            return res;
        } catch (DecodeException ex) {
            return DataResult.error(ex);
        }
    }

    @Override
    public final <U> DataResult<U> encode(E input, DynamicOps<U> ops, U prefix) {
        CodecType<?> type = input.getType();
        @SuppressWarnings("unchecked")
        Codec<E> codec = (Codec<E>) type.codec();

        DataResult<U> innerResult = codec.encode(input, ops, prefix);
        return innerResult.flatMap(innerMap -> {
            var builder = ops.mapBuilder();
            builder.add("type", type.getName(), Codec.STRING);
            var mapRes = ops.getMap(innerMap);
            if (mapRes.error().isPresent()) {
                return DataResult.error(() -> "innerCodec result not a map: type=" + type.getName());
            }
            mapRes.getOrThrow().entries().forEach(e -> builder.add(e.getFirst(), e.getSecond()));
            return builder.build(prefix);
        });
    }
}
