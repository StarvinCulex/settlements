package com.outlook.hxsw.settlements.engine.data.utils;

public abstract class WithParentAndID<P extends SidecarData> extends WithParent<P> implements Comparable<WithParentAndID<?>> {
    public final int id;

    public WithParentAndID(int id) {
        this.id = id;
    }

    public final int getID() {
        return id;
    }

    @Override
    public int compareTo(WithParentAndID<?> other) {
        return Integer.compare(this.id, other.id);
    }
}
