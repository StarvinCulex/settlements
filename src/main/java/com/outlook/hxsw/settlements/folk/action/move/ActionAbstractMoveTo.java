package com.outlook.hxsw.settlements.folk.action.move;

import com.outlook.hxsw.settlements.SettlementsMain;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;
import com.outlook.hxsw.settlements.entities.folk.FolkAction;

public abstract class ActionAbstractMoveTo<A> extends FolkAction<A> {
    protected final FolkPos destination;
    protected int accuracy = 1;
    protected double speed = 1;
    protected ActionAbstractMoveTo(FolkPos destination) {
        this.destination = destination;
    }

    protected abstract A generateOutput();

    @Override
    protected void start() {
        var targets = destination.actionPoints(entity().level());
        if (targets.isEmpty()) {
            SettlementsMain.LOGGER.warn("AbstractMoveTo.start failed. dest={}", destination);
            return;
        }

        var path = entity().getNavigation().createPath(targets, accuracy);
        entity().getNavigation().moveTo(path, speed);
    }

    @Override
    protected void interrupt() {
        entity().getNavigation().stop();
    }

    @Override
    protected void tick() {
        if (entity().getNavigation().isDone()) {
            complete(generateOutput());
        }
    }

    @Override
    protected void cancel() {
    }
}
