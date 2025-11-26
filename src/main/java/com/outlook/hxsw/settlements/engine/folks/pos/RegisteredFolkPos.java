package com.outlook.hxsw.settlements.engine.folks.pos;

import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.data.codecs.DynamicCodecer;

public final class RegisteredFolkPos extends DynamicCodecer<FolkPos, CodecType<? extends FolkPos>> {
    public static final RegisteredFolkPos INSTANCE = new RegisteredFolkPos();

    private RegisteredFolkPos() {
        register(InBuilding.Type.INSTANCE);
    }
}
