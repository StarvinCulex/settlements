package com.outlook.hxsw.settlements.utils.grid;


import net.minecraft.world.level.block.Rotation;

public final class GridCellPattern implements GridsPattern<GridCell> {
    private GridCellPattern() {}

    public static GridCellPattern INSTANCE = new GridCellPattern();

    public static GridCellPattern of() {
        return INSTANCE;
    }

    @Override
    public GridCell offset(Grid g) {
        return new GridCell(g);
    }

    @Override
    public GridCellPattern rotate(Rotation rotation) {
        return this;
    }
}
