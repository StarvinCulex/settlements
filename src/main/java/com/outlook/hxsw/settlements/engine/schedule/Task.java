package com.outlook.hxsw.settlements.engine.schedule;

@FunctionalInterface
public non-sealed interface Task<S, R> extends AsyncScheduledTask.TaskSlot<S, R> {
    R run(S scheduler);
}
