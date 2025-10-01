package com.outlook.hxsw.settlements.engine.folks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.utils.ChildContainer;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import java.util.List;

public final class FolkSet extends ChildContainer<Folk, SettlementsData> {
    public FolkSet() {}

    public FolkSet(int nextChildID, List<Folk> children) {
        super(nextChildID);
        addAll(children);
    }

    @Override
    public void registerToDataScheduler(DataScheduler scheduler) {

    }

    public static final Codec<FolkSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("nextChildID").forGetter(FolkSet::getNextChildID),
            Codec.list(Folk.CODEC).fieldOf("children").forGetter(FolkSet::getChildrenList)
    ).apply(instance, FolkSet::new));
}
