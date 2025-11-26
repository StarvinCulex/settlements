package com.outlook.hxsw.settlements.building.agriculture;

import com.outlook.hxsw.settlements.building.utils.BasicBuilding;
import com.outlook.hxsw.settlements.building.utils.StructurePattern;
import com.outlook.hxsw.settlements.building.utils.filter.FacingArgument;
import com.outlook.hxsw.settlements.commercial.market.WithStockpile;
import com.outlook.hxsw.settlements.engine.buildings.BuildingLocation;
import com.outlook.hxsw.settlements.engine.grid.GridRegion;
import com.outlook.hxsw.settlements.engine.folks.job.WithWorkGroup;

import java.util.function.Supplier;

public abstract class HarvesterBuilding<P extends Enum<P> & Supplier<StructurePattern>>
        extends BasicBuilding<P> implements WithStockpile, WithWorkGroup<HarvesterBuilding<?>> {
    protected HarvesterBuilding(int id, P pattern, FacingArgument<BuildingLocation<GridRegion>> location, String name) {
        super(id, pattern, location, name);
    }

    protected HarvesterBuilding(Class<P> variantClass, BasicProperties properties) {
        super(variantClass, properties);
    }
}
