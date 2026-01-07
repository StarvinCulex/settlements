package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public sealed abstract class SidecarData permits WithParent, ManualSidecarData {
    private DataScheduler dataScheduler;

    public final DataScheduler scheduler() {
        return dataScheduler;
    }

    protected void onRegistering(DataScheduler scheduler) {}

    void register(DataScheduler scheduler) {
        dataScheduler = scheduler;
        registerChildren();
        onRegistering(scheduler);
    }

    void registerChildren() {
        List<Field> fields = getChildrenFields(getClass());
        try {
            for (Field field : fields) {
                WithParent<?> child = (WithParent<?>) field.get(this);
                child.registerParent(this);
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();
    static {
        fieldCache.put(SidecarData.class, List.of());
    }

    private static List<Field> getChildrenFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, SidecarData::directGetChildrenFields);
    }

    private static List<Field> directGetChildrenFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(getChildrenFields(clazz.getSuperclass()));
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(Child.class)) {
                if (!Modifier.isFinal(field.getModifiers())) {
                    throw new IllegalArgumentException(
                            "field " + field.getName() + " in class " + clazz.getName() + " is not final."
                    );
                }
                fields.add(field);
            }
        }
        return Collections.unmodifiableList(fields);
    }
}
