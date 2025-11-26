package com.outlook.hxsw.settlements.engine.data.utils;

import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import java.lang.reflect.Field;
import java.util.*;

public abstract class SidecarData {
    public abstract DataScheduler scheduler();

    protected void registerToDataScheduler(DataScheduler scheduler) {}

    void onRegistered(DataScheduler scheduler) {
        List<Field> fields = getChildrenFields(getClass());
        try {
            for (Field field : fields) {
                SidecarData child = (SidecarData) field.get(this);
                child.registeredByParent(this);
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        registerToDataScheduler(scheduler);
    }

    void registeredByParent(SidecarData parent) {
        onRegistered(parent.scheduler());
    }

    private static final Map<Class<?>, List<Field>> fieldCache = new HashMap<>();

    private static List<Field> getChildrenFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, SidecarData::directGetChildrenFields);
    }

    private static List<Field> directGetChildrenFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(Child.class)) {
                fields.add(field);
            }
        }
        fields.addAll(getChildrenFields(clazz.getSuperclass()));
        return Collections.unmodifiableList(fields);
    }
}
