package com.outlook.hxsw.settlements.engine.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class GridRegion implements Grids {
    private final Grid nwCorner;
    private final Grid seCorner;

    public GridRegion(Grid northwestCorner, Grid southeastCorner) {
        this.nwCorner = northwestCorner;
        this.seCorner = southeastCorner;
    }

    public GridRegion(Grid northwestCorner, GridSize size) {
        this(northwestCorner, new Grid(northwestCorner.u + size.getXGrid() - 1, northwestCorner.v + size.getZGrid() - 1));
    }

    public GridRegion(int north, int west, int south, int east) {
        this(new Grid(north, west), new Grid(south, east));
    }

    public GridRegion(Grid center, int gridDistance) {
        this(
                new Grid(center.u - gridDistance, center.v - gridDistance),
                new Grid(center.u + gridDistance, center.v + gridDistance)
        );
    }

    @Override
    public GridRegion nextTo(GridSide side) {
        return switch (side) {
            case SOUTH -> new GridRegion(getSouth() + 1, getWest(), getSouth() + 1, getEast());
            case WEST -> new GridRegion(getNorth(), getWest() - 1, getSouth(), getWest() - 1);
            case NORTH -> new GridRegion(getNorth() - 1, getWest(), getNorth() - 1, getEast());
            case EAST -> new GridRegion(getNorth(), getEast() + 1, getSouth(), getEast() + 1);
        };
    }

    public Grid getNWCorner() {
        return nwCorner;
    }

    public Grid getSECorner() {
        return seCorner;
    }

    @Override
    public int getWest() {
        return nwCorner.u;
    }

    @Override
    public int getEast() {
        return seCorner.u;
    }

    @Override
    public int getNorth() {
        return nwCorner.v;
    }

    @Override
    public int getSouth() {
        return seCorner.v;
    }

    @Override
    public boolean contains(Grid g) {
        return getWest() <= g.u && g.u <= getEast() &&
                getNorth() <= g.v && g.v <= getSouth();
    }

    @Override
    public boolean intersects(Grids g) {
        switch (g) {
            case GridRegion grids -> {
                int aLeft = nwCorner.u, aRight = seCorner.u;
                int aTop = nwCorner.v, aBottom = seCorner.v;
                int bLeft = grids.nwCorner.u, bRight = grids.seCorner.u;
                int bTop = grids.nwCorner.v, bBottom = grids.seCorner.v;

                boolean horizontalOverlap = aLeft <= bRight && bLeft <= aRight;
                boolean verticalOverlap = aTop <= bBottom && bTop <= aBottom;
                return horizontalOverlap && verticalOverlap;
            }
            default -> throw new UnsupportedOperationException("Not supported for this Grids type");
        }
    }

    @Override
    public Grid begin() {
        return nwCorner;
    }

    @Override
    public Grid center() {
        return new Grid((this.getWest() + this.getEast()) / 2, (this.getNorth() + this.getSouth()) / 2);
    }

    @Override
    public GridSize getPattern() {
        return GridSize.of(this.getEast() - this.getWest() + 1, this.getSouth() - this.getNorth() + 1);
    }

    @Override
    public Iterator<Grid> iterator() {
        return new Iter();
    }

    @Override
    public Stream<Grid> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator(), 0),
                false
        );
    }

    @Override
    public String toString() {
        return String.format("region{nw=(%s),se=(%s)}", this.getNWCorner(), this.getSECorner());
    }

    public static final Codec<GridRegion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
           Grid.CODEC.fieldOf("nwCorner").forGetter(GridRegion::getNWCorner),
           Grid.CODEC.fieldOf("seConrer").forGetter(GridRegion::getSECorner)
    ).apply(instance, GridRegion::new));


    class Iter implements Iterator<Grid> {
        private Grid grid;

        Iter() {
            grid = new Grid(getWest() - 1, getNorth());
        }

        @Override
        public boolean hasNext() {
            return !(grid.u == getEast() && grid.v == getSouth());
        }

        @Override
        public Grid next() {
            if (grid.u < getEast()) {
                grid = new Grid(grid.u + 1, grid.v);
            } else if (grid.v < getSouth()) {
                grid = new Grid(getWest(), grid.v + 1);
            } else {
                throw new NoSuchElementException();
            }
            return grid;
        }
    }
}
