package com.outlook.hxsw.settlements.engine.data.utils;

import java.util.*;

@SuppressWarnings("rawtypes")
public abstract class ChildContainer<C extends WithParentAndID, P extends SidecarData> extends WithParent<P> {
    private int nextChildID = 1;
    private final Map<Integer, C> children = new HashMap<>();

    public ChildContainer(Class<P> parentClass) {
        super(parentClass);
    }

    public ChildContainer(Class<P> parentClass, int nextChildID, Collection<C> children) {
        super(parentClass);
        this.nextChildID = nextChildID;
        addAll(children);
    }

    public Optional<C> get(int id) {
        return Optional.ofNullable(children.get(id));
    }

    public List<C> getChildrenList() {
        return children.values().stream().sorted().toList();
    }

    public Map<Integer, C> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    protected int getNextChildID() {
        return nextChildID;
    }

    protected int generateChildID() {
        return nextChildID++;
    }

    protected void add(C child) {
        if (children.putIfAbsent(child.id, child) != null) {
            throw new IllegalArgumentException("duplicated id " + child.id);
        }
        if (scheduler() != null) {
            child.registerParent(this);
        }
    }

    protected void addAll(Collection<C> children) {
        children.forEach(this::add);
    }

    protected void remove(int id) {
        var child = children.remove(id);
        if (child != null) {
            child.onRemove();
        }
    }

    @Override
    void registerChildren() {
        super.registerChildren();
        for (C child : children.values()) {
            child.registerParent(this);
        }
    }
}
