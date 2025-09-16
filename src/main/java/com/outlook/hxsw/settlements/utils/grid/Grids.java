package com.outlook.hxsw.settlements.utils.grid;

import java.util.stream.Stream;

public sealed interface Grids extends Iterable<Grid> permits GridRegion, GridCell {
    boolean contains(Grid g);
    boolean intersects(Grids other);
    Grid begin();
    Grid center();
    int getWest();
    int getEast();
    int getNorth();
    int getSouth();
    Grids nextTo(GridSide facing);
    GridsPattern<?> getPattern();
    Stream<Grid> stream();
}
