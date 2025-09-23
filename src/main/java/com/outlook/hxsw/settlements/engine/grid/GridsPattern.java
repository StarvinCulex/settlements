package com.outlook.hxsw.settlements.engine.grid;

import net.minecraft.world.level.block.Rotation;

public interface GridsPattern<G extends Grids> {
    G offset(Grid g);

    GridsPattern<G> rotate(Rotation rotation);
}
