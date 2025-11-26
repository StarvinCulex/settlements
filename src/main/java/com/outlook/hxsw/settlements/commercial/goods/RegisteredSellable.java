package com.outlook.hxsw.settlements.commercial.goods;

import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.data.codecs.DynamicCodecer;

public final class RegisteredSellable extends DynamicCodecer<Sellable, CodecType<? extends Sellable>> {
    public static final RegisteredSellable INSTANCE = new RegisteredSellable();

    private RegisteredSellable() {
        register(GoodsItem.Type.INSTANCE);
    }
}
