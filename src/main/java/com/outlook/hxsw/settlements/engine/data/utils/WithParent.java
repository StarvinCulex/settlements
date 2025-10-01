package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

public abstract class WithParent<P extends SidecarData> implements SidecarData {
    private P parent = null;

    /**
     * 可能是null。
     */
    public final P getParent() {
        return parent;
    }

    public final void setParent(P parent) {
        if (this.parent != null) {
            throw new RuntimeException("Can't set parent twice");
        }
        this.parent = parent;
        if (scheduler() != null) {
            this.registerToDataScheduler(scheduler());
        }
    }

    /**
     * 可能是null
     */
    @Override
    public final DataScheduler scheduler() {
        return parent == null ? null : parent.scheduler();
    }
}
