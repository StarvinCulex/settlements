package com.outlook.hxsw.settlements.folk.action.build;

import com.outlook.hxsw.settlements.entities.folk.FolkEntity;

interface BuildStep {
    void start(BasicBuildAction buildAction, FolkEntity entity);

    void tick(BasicBuildAction buildAction, FolkEntity entity);

    void stop(BasicBuildAction buildAction, FolkEntity entity);
}
