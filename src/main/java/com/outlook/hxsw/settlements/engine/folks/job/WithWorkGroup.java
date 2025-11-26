package com.outlook.hxsw.settlements.engine.folks.job;

import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;

public interface WithWorkGroup<P extends SidecarData> {
    WorkGroup<? extends P> getWorkGroup();
}
