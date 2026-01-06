package com.outlook.hxsw.settlements.folk.action.build;

import com.outlook.hxsw.settlements.entities.folk.FolkAction;

import javax.annotation.Nullable;
import java.util.List;

public abstract class BasicBuildAction extends FolkAction<Void> {
    private List<BuildStep> steps;
    private int stage = 0;

    public final int getSteps() {
        return steps != null ? steps.size() : 1;
    }

    public final int getStage() {
        return stage;
    }

    BasicBuildAction() {}

    abstract List<BuildStep> generate();

    /**
     * 这个方法给BuildStep调用。
     */
    final void nextStep() {
        if (++stage >= steps.size()) {
            complete(null);
            return;
        }
        current().start(this, entity());
    }

    private BuildStep current() {
        return steps.get(stage);
    }

    @Override
    protected void start() {
        if (steps == null) {
            steps = generate();
        }
        current().tick(this, entity());
    }

    @Override
    protected void tick() {
        current().tick(this, entity());
    }

    @Override
    protected void interrupt() {
        current().stop(this, entity());
    }

    @Override
    protected void cancel() {
    }
}
