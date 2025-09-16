package com.outlook.hxsw.settlements.engine.buildings;

import com.outlook.hxsw.settlements.utils.grid.Grid;

public class GridOccupiedException extends RuntimeException {
    public final Grid grid;
    public final Buildable buildingToSet;
    public final Buildable occupiedBuilding;


    public GridOccupiedException(Grid grid, Buildable buildingToSet, Buildable occupiedBuilding) {
        super("%s occupied by %s".formatted(grid, occupiedBuilding));
        this.grid = grid;
        this.buildingToSet = buildingToSet;
        this.occupiedBuilding = occupiedBuilding;
    }
}
