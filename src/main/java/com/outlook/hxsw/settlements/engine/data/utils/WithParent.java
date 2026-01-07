package com.outlook.hxsw.settlements.engine.data.utils;

public abstract non-sealed class WithParent<P extends SidecarData> extends SidecarData {
    private P parent = null;
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

    final void registerParent(SidecarData parent) {
        if (this.parent != null) {
            throw new RuntimeException("Can't set parent twice");
        }
        P p = parentClass.cast(parent);
        checkedRegisterParent(p);
    }

    final void checkedRegisterParent(P parent) {
        this.parent = parent;
        register(parent.scheduler());
    }
}
