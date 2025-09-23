package com.outlook.hxsw.settlements.engine.goods;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import com.mojang.serialization.Codec;

public record ItemGoods(Holder<Item> item) implements Sellable {
    public static final Codec<ItemGoods> CODEC = Item.CODEC.xmap(ItemGoods::new, ItemGoods::item);
}
