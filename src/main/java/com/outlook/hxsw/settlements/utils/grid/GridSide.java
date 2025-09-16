package com.outlook.hxsw.settlements.utils.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.Optional;

public enum GridSide {
    SOUTH,
    WEST,
    NORTH,
    EAST;

    public GridRelativeSide angle(GridSide other) {
        return GridRelativeSide.values()[(other.ordinal() - ordinal()) & 3];
    }

    public GridSide rotate(GridRelativeSide side) {
        return GridSide.values()[(ordinal() + side.ordinal()) & 3];
    }

    public GridSide getClockWise() {
        return switch (this) {
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case NORTH -> SOUTH;
            case EAST -> WEST;
        };
    }

    public static Optional<GridSide> of(Direction direction) {
        return switch (direction) {
            case SOUTH -> Optional.of(SOUTH);
            case WEST -> Optional.of(WEST);
            case NORTH -> Optional.of(NORTH);
            case EAST -> Optional.of(EAST);
            default -> Optional.empty();
        };
    }

    public static final Codec<GridSide> CODEC = Codec.string(4, 5).xmap(GridSide::valueOf, GridSide::name);

    public static final Keyable KEYABLE = Keyable.forStrings(() -> Arrays.stream(GridSide.values()).map(GridSide::name));
}
