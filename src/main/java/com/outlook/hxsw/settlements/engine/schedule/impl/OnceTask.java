package com.outlook.hxsw.settlements.engine.schedule.impl;

@FunctionalInterface
interface OnceTask<S> {
    void tryExecute(S scheduler);
}
