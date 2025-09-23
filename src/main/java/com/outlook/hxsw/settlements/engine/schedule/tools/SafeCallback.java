package com.outlook.hxsw.settlements.engine.schedule.tools;

import com.outlook.hxsw.settlements.engine.schedule.Scheduler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class SafeCallback<T> implements Consumer<Scheduler.Server> {
    private final T callbackResource;
    private final Function<Scheduler.Server, BiConsumer<Scheduler.Data, T>> task;

    public SafeCallback(T resource, Function<Scheduler.Server, BiConsumer<Scheduler.Data, T>> task) {
        this.callbackResource = resource;
        this.task = task;
    }

    public SafeCallback(T resource, Consumer<Scheduler.Server> task, BiConsumer<Scheduler.Data, T> callback) {
        this(resource, s -> {
            task.accept(s);
            return callback;
        });
    }

    @Override
    public void accept(Scheduler.Server scheduler) {
        var callbackFunction = task.apply(scheduler);
        scheduler.run(s -> callbackFunction.accept(s, callbackResource));
    }
}
