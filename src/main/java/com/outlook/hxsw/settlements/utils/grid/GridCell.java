package com.outlook.hxsw.settlements.utils.grid;

import com.mojang.serialization.Codec;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public record GridCell(Grid at) implements Grids {
    @Override
    public GridCell nextTo(GridSide side) {
        Grid next = switch (side) {
            case SOUTH -> new Grid(at.u, at.v + 1);
            case WEST -> new Grid(at.u - 1, at.v);
            case NORTH -> new Grid(at.u, at.v - 1);
            case EAST -> new Grid(at.u + 1, at.v);
        };
        return new GridCell(next);
    }

    @Override
    public boolean contains(Grid g) {
        return begin().equals(g);
    }

    @Override
    public boolean intersects(Grids g) {
        return g.contains(begin());
    }

    @Override
    public Grid begin() {
        return at;
    }

    @Override
    public Grid center() {
        return begin();
    }

    @Override
    public int getWest() {
        return at.u;
    }

    @Override
    public int getEast() {
        return at.u;
    }

    @Override
    public int getNorth() {
        return at.v;
    }

    @Override
    public int getSouth() {
        return at.v;
    }

    @Override
    public GridCellPattern getPattern() {
        return GridCellPattern.INSTANCE;
    }

    @Override
    public Stream<Grid> stream() {
        return Stream.of(at);
    }

    @Override
    public Iterator<Grid> iterator() {
        return List.of(at).iterator();
    }

    @Override
    public String toString() {
        return String.format("cell(%s)", this.begin());
    }

    public static Codec<GridCell> CODEC = Grid.CODEC.xmap(GridCell::new, GridCell::begin);
}
