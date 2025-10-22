package com.outlook.hxsw.settlements.engine.schedule;

@FunctionalInterface
public interface VoidTask<S> {
    void run(S scheduler);
}
