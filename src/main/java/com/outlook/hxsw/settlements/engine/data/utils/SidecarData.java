package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

public interface SidecarData {
    void registerToDataScheduler(DataScheduler scheduler);
    DataScheduler scheduler();
}
