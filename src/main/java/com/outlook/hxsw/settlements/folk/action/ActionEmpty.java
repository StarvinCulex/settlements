package com.outlook.hxsw.settlements.folk.action;

import com.outlook.hxsw.settlements.entities.folk.FolkAction;


public final class ActionEmpty<A> extends FolkAction<A> {
    private final A output;
    public ActionEmpty(A output) {
        this.output = output;
    }

    @Override
    protected void start() {

    }

    @Override
    protected void tick() {
        complete(output);
    }

    @Override
    public void interrupt() {

    }

    @Override
    protected void cancel() {

    }
}
