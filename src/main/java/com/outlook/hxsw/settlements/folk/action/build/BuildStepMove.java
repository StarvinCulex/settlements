package com.outlook.hxsw.settlements.folk.action.build;

import com.outlook.hxsw.settlements.entities.folk.FolkEntity;
import net.minecraft.core.BlockPos;

/**
 * 移动到这个方块的最近处。
 * @param target
 */
record BuildStepMove(BlockPos target) implements BuildStep {
    @Override
    public void start(BasicBuildAction buildAction, FolkEntity entity) {
        var path = entity.getNavigation().createPath(target, 2);
        entity.getNavigation().moveTo(path, 1);
    }

    @Override
    public void tick(BasicBuildAction buildAction, FolkEntity entity) {
        if (entity.getNavigation().isDone()) {
            buildAction.nextStep();
        }
    }

    @Override
    public void stop(BasicBuildAction buildAction, FolkEntity entity) {
        entity.getNavigation().stop();
    }
}
