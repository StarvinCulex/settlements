package com.outlook.hxsw.settlements.engine.data.utils;

@SuppressWarnings("rawtypes")
public abstract class WithParentAndID<P extends ChildContainer> extends WithParent<P> implements Comparable<WithParentAndID<?>> {
    public final int id;

    public WithParentAndID(Class<P> parentClass, int id) {
        super(parentClass);
        this.id = id;
    }

    public final int getID() {
        return id;
    }

    protected void onRemove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(WithParentAndID<?> other) {
        return Integer.compare(this.id, other.id);
    }
}
