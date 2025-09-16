package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.Scheduler;

import javax.annotation.Nullable;

public abstract class WithParent<P extends SidecarData> implements SidecarData {
    private @Nullable P parent = null;

    public final @Nullable P getParent() {
        return parent;
    }

    public final void setParent(P parent) {
        if (this.parent != null) {
            throw new RuntimeException("Can't set parent twice");
        }
        this.parent = parent;
        if (scheduler() != null) {
            this.registerToSidecar(scheduler());
        }
    }

    @Override
    public final @Nullable Scheduler.Sidecar scheduler() {
        return parent == null ? null : parent.scheduler();
    }
}
