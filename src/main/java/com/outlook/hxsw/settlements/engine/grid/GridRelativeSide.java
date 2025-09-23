package com.outlook.hxsw.settlements.engine.grid;

import com.mojang.serialization.Codec;

public enum GridRelativeSide {
    FRONT,
    RIGHT,
    BACK,
    LEFT;

    public GridRelativeSide getClockWise() {
        return switch (this) {
            case FRONT -> BACK;
            case RIGHT -> LEFT;
            case BACK -> FRONT;
            case LEFT -> RIGHT;
        };
    }

    public static Codec<GridRelativeSide> CODEC = Codec.string(4, 5).xmap(GridRelativeSide::valueOf, GridRelativeSide::name);
}
