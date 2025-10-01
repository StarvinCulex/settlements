package com.outlook.hxsw.settlements.engine.schedule;

import java.util.function.Supplier;

@FunctionalInterface
public interface Task<S, R> extends Supplier<Task<S, R>> {
    R run(S scheduler);

    @Override
    default Task<S, R> get() {
        return this;
    }
}
