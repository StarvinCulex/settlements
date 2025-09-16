package com.outlook.hxsw.settlements.goods;

import com.mojang.serialization.Codec;

public sealed interface Sellable permits ItemGoods {
    Codec<Sellable> CODEC = ItemGoods.CODEC.xmap(x -> x, x -> (ItemGoods) x);
}
