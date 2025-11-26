package com.outlook.hxsw.settlements.commercial.goods;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecElement;

public interface Sellable extends CodecElement {
    Codec<Sellable> CODEC = RegisteredSellable.INSTANCE;
}
