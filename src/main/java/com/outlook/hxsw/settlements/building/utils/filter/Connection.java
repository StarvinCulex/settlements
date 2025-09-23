package com.outlook.hxsw.settlements.building.utils.filter;


import com.outlook.hxsw.settlements.building.utils.BasicBuilding;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.grid.GridRelativeSide;
import com.outlook.hxsw.settlements.engine.grid.GridSide;

public record Connection(
        GridRelativeSide relativeSide,
        GridSide direction,
        int alignment,
        Building building
) {
    public GridRelativeSide pointedSide() {
        if (building instanceof BasicBuilding<?> b) {
            return b.getFacing().angle(direction.getClockWise());
        }
        return GridSide.SOUTH.angle(direction.getClockWise());
    }

    public String toString() {
        return String.format("(%s,%s,%d,%s)", relativeSide, direction, alignment, building);
    }
}