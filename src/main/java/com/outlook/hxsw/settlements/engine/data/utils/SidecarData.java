package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.Scheduler;

public interface SidecarData {
    void registerToSidecar(Scheduler.Sidecar sidecar);
    Scheduler.Sidecar scheduler();
}
