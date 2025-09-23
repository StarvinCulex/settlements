package com.outlook.hxsw.settlements.engine.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;
import com.outlook.hxsw.settlements.engine.folks.FolkSet;

public final class SettlementsData implements SidecarData {
    private Scheduler.Data dataScheduler;

    public final TownSet towns;
    public final FolkSet folks;

    SettlementsData(TownSet towns, FolkSet folks) {
        this.towns = towns;
        towns.setParent(this);

        this.folks = folks;
        folks.setParent(this);
    }

    SettlementsData() {
        this(new TownSet(), new FolkSet());
    }

    @Override
    public void registerToSidecar(Scheduler.Data scheduler) {
        this.dataScheduler = scheduler;
        this.towns.registerToSidecar(scheduler);
    }

    @Override
    public Scheduler.Data scheduler() {
        return dataScheduler;
    }

    public static final Codec<SettlementsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TownSet.CODEC.fieldOf("towns").forGetter(d -> d.towns),
                    FolkSet.CODEC.fieldOf("folks").forGetter(d -> d.folks)
            ).apply(instance, SettlementsData::new)
    );
}
