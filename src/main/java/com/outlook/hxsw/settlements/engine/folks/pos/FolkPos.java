package com.outlook.hxsw.settlements.engine.folks.pos;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecElement;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import com.outlook.hxsw.settlements.engine.schedule.Condition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.Set;

/**
 * 是一个不可变对象。多线程安全。
 */
public sealed interface FolkPos extends CodecElement permits InBuilding {
    ResourceKey<Level> dimension();
    Grid grid();
    Optional<BlockPos> spawnPoint(Level level);
    Set<BlockPos> actionPoints(Level level);
    Condition actingCondition();

    Codec<FolkPos> CODEC = RegisteredFolkPos.INSTANCE;
}

