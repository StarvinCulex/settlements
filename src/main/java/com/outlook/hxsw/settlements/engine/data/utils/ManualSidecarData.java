package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

public abstract non-sealed class ManualSidecarData extends SidecarData {
    @Override
    protected void register(DataScheduler scheduler) {
        super.register(scheduler);
    }
}
