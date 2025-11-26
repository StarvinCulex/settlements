package com.outlook.hxsw.settlements.commercial.goods;

import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import com.mojang.serialization.Codec;

public record GoodsItem(Holder<Item> item) implements Sellable {
    public static GoodsItem of(Item item) {
        return new GoodsItem(Holder.direct(item));
    }

    static final class Type implements CodecType<GoodsItem> {
        public static final Type INSTANCE = new Type();

        @Override
        public String getName() {
            return "item";
        }

        @Override
        public Codec<GoodsItem> codec() {
            return Item.CODEC.xmap(GoodsItem::new, GoodsItem::item);
        }
    }

    @Override
    public CodecType<GoodsItem> getType() {
        return Type.INSTANCE;
    }
}
