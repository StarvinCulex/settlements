package com.outlook.hxsw.settlements.engine.buildings;

import com.outlook.hxsw.settlements.engine.grid.*;

import java.util.stream.Stream;

public interface BuildingFactory<B extends Building, G extends Grids, X extends BuildingArgument> {
    /**
     * 获取此工厂方法构造出建筑的占地区域。每个工厂实例创建出来的建筑的占地区域都是此样式。
     * @return 建筑的占地区域样式。每个BuildingFactory实例返回的结果永远相同。
     */
    GridsPattern<G> getGridsPattern();

    Stream<X> filter(BuildingSet buildingSet, Stream<Grid> requiringGrids);

    B make(int id, X args);
}
