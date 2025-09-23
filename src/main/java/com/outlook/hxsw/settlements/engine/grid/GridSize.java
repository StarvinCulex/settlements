package com.outlook.hxsw.settlements.engine.grid;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Rotation;

import java.util.Objects;

public class GridSize implements GridsPattern<GridRegion> {
    private final int u;
    private final int v;

    private static final int CACHE_MAX = 5;
    private static final GridSize[][] CACHE = new GridSize[CACHE_MAX + 1][CACHE_MAX + 1];

    static {
        for (int i = 1; i <= CACHE_MAX; i++) {
            for (int j = 1; j <= CACHE_MAX; j++) {
                CACHE[i][j] = new GridSize(i, j);
            }
        }
    }

    public static GridSize of(int xGrid, int zGrid) {
        int u = Math.max(xGrid, 1), v = Math.max(zGrid, 1);
        if (u <= CACHE_MAX && v <= CACHE_MAX) return CACHE[u][v];
        return new GridSize(xGrid, zGrid);
    }

    GridSize(int xGrid, int zGrid) {
        this.u = xGrid;
        this.v = zGrid;
    }

    public int getXGrid() {
        return u;
    }

    public int getZGrid() {
        return v;
    }

    public int getX() {
        return u << 3;
    }

    public int getZ() {
        return u << 3;
    }

    @Override
    public GridRegion offset(Grid g) {
        return new GridRegion(g, this);
    }

    @Override
    public GridSize rotate(Rotation rotation) {
        return switch (rotation) {
            case NONE, CLOCKWISE_180 -> this;
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> GridSize.of(getZGrid(), getXGrid());
        };
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GridSize g && getXGrid() == g.getXGrid() && getZGrid() == g.getZGrid();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getXGrid(), getZGrid());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("xGrid", this.getXGrid()).add("zGrid", this.getZGrid()).toString();
    }

    public static final Codec<GridSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("xGrid").forGetter(GridSize::getXGrid),
            Codec.INT.fieldOf("zGrid").forGetter(GridSize::getZGrid)
    ).apply(instance, GridSize::of));
}
