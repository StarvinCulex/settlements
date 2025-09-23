package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.Scheduler;

import java.util.*;

public class ChildContainer<C extends WithParentAndID, P extends SidecarData> extends WithParent<P> {
    private int nextChildID = 1;
    private final Map<Integer, C> children = new HashMap<>();

    public ChildContainer() {}

    public ChildContainer(int nextChildID) {
        this.nextChildID = nextChildID;
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
        child.setParent(this);
    }

    protected void addAll(Collection<C> children) {
        children.forEach(this::add);
    }

    @Override
    public void registerToSidecar(Scheduler.Data scheduler) {
        children.values().forEach(c -> c.registerToSidecar(scheduler));
    }
}
