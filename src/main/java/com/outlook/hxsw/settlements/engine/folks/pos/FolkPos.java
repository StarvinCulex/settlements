package com.outlook.hxsw.settlements.engine.folks.pos;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecElement;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public sealed interface FolkPos extends CodecElement permits InBuilding {
    ResourceKey<Level> dimension();
    Grid grid();

    Codec<FolkPos> CODEC = RegisteredFolkPos.INSTANCE;
}

