package com.outlook.hxsw.settlements.engine.buildings;

import com.outlook.hxsw.settlements.engine.grid.Grid;

public class GridOccupiedException extends RuntimeException {
    public final Grid grid;
    public final Building buildingToSet;
    public final Building occupiedBuilding;


    public GridOccupiedException(Grid grid, Building buildingToSet, Building occupiedBuilding) {
        super("%s occupied by %s".formatted(grid, occupiedBuilding));
        this.grid = grid;
        this.buildingToSet = buildingToSet;
        this.occupiedBuilding = occupiedBuilding;
    }
}
