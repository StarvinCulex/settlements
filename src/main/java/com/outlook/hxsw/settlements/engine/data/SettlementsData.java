package com.outlook.hxsw.settlements.engine.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;
import com.outlook.hxsw.settlements.engine.data.utils.Child;
import com.outlook.hxsw.settlements.engine.folks.FolkSet;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

public final class SettlementsData extends SidecarData {
    private DataScheduler dataScheduler;

    public final SettlementsTable syncTable;

    public final @Child TownSet towns;
    public final @Child FolkSet folks;

    SettlementsData(TownSet towns, FolkSet folks) {
        this.towns = towns;
        this.folks = folks;
        this.syncTable = new SettlementsTable(folks.zoneTable);
    }

    SettlementsData() {
        this(new TownSet(), new FolkSet());
    }

    @Override
    protected void registerToDataScheduler(DataScheduler scheduler) {
        this.dataScheduler = scheduler;
        super.registerToDataScheduler(scheduler);
    }

    @Override
    public DataScheduler scheduler() {
        return dataScheduler;
    }

    public static final Codec<SettlementsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TownSet.CODEC.fieldOf("towns").forGetter(d -> d.towns),
                    FolkSet.CODEC.fieldOf("folks").forGetter(d -> d.folks)
            ).apply(instance, SettlementsData::new)
    );
}
