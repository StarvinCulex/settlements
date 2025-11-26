package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

public abstract class WithParent<P extends SidecarData> extends SidecarData {
    private P parent = null;
    private DataScheduler scheduler;
    private final Class<P> parentClass;

    public WithParent(Class<P> parentClass) {
            this.parentClass = parentClass;
    }

    /**
     * 可能是null。
     */
    public final P getParent() {
        return parent;
    }

    @Override
    void registeredByParent(SidecarData parent) {
        P p = parentClass.cast(parent);
        if (this.parent != null) {
            throw new RuntimeException("Can't set parent twice");
        }
        this.parent = p;
        this.scheduler = p.scheduler();
        super.registeredByParent(parent);
    }

    @Override
    public final DataScheduler scheduler() {
        return scheduler;
    }
}
