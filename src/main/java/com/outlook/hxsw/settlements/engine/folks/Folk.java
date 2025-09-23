package com.outlook.hxsw.settlements.engine.folks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.WithParentAndID;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;

public final class Folk extends WithParentAndID<FolkSet> {
    public Folk(int id) {
        super(id);
    }

    @Override
    public void registerToSidecar(Scheduler.Data scheduler) {

    }

    public static final Codec<Folk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(Folk::getID)
    ).apply(instance, Folk::new));
}
