package com.outlook.hxsw.settlements.engine.data.codecs;

import com.mojang.serialization.Codec;

public interface CodecType<E extends CodecElement> {
    String getName();
    Codec<E> codec();
}
