package com.outlook.hxsw.settlements.engine.data.id;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.data.utils.ChildContainer;
import com.outlook.hxsw.settlements.engine.data.utils.WithParentAndID;

import java.util.Optional;

public class ID<P extends ChildContainer<? super C, ?>, C extends WithParentAndID<P>> {
    public final int value;

    ID(int value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public final Optional<C> get(P childContainer) {
        return (Optional<C>) childContainer.get(value);
    }

    public final Optional<C> get(P childContainer, Class<C> clazz) {
        return childContainer.get(value).map(clazz::cast);
    }

    public boolean isEmpty() {
        return value == 0;
    }

    public static <P extends ChildContainer<? super C, ?>, C extends WithParentAndID<P>>
    ID<P, C> empty() {
        return make(0);
    }

    public static <P extends ChildContainer<? super C, ?>, C extends WithParentAndID<P>>
    ID<P, C> of(C child) {
        return make(child.id);
    }

    @SuppressWarnings("rawtypes")
    public static final Codec<ID> CODEC = Codec.INT.xmap(ID::make, id -> id.value);

    @Override
    public boolean equals(Object other) {
        return other instanceof ID<?, ?> id && value == id.value;
    }

    @Override
    public final int hashCode() {
        return value;
    }

    @SuppressWarnings("rawtypes")
    private static final ID[] CACHE = new ID[256];
    static {
        for (int i = 0; i < CACHE.length; ++i) {
            @SuppressWarnings("rawtypes")
            ID id = new ID(i);
            CACHE[i] = id;
        }
    }
    static <P extends ChildContainer<? super C, ?>, C extends WithParentAndID<P>>
    ID<P, C> make(int value) {
        if (0 <= value && value < CACHE.length) {
            @SuppressWarnings("unchecked")
            ID<P, C> id = CACHE[value];
            return id;
        }
        return new ID<>(value);
    }
}
