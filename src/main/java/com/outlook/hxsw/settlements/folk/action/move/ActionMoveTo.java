package com.outlook.hxsw.settlements.folk.action.move;

import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;

public final class ActionMoveTo extends ActionAbstractMoveTo<Void> {
    public ActionMoveTo(FolkPos destination) {
        super(destination);
    }

    @Override
    protected Void generateOutput() {
        return null;
    }
}
