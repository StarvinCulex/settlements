package com.outlook.hxsw.settlements.engine.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.Child;
import com.outlook.hxsw.settlements.engine.data.utils.ManualSidecarData;
import com.outlook.hxsw.settlements.engine.folks.FolkSet;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

public final class SettlementsData extends ManualSidecarData {
    public final SettlementsTable syncTable;

    public final @Child TownSet towns;
    public final @Child FolkSet folks;

    private SettlementsData(TownSet towns, FolkSet folks) {
        this.towns = towns;
        this.folks = folks;
        this.syncTable = new SettlementsTable(folks.zoneTable);
    }

    @Override
    protected void register(DataScheduler scheduler) { // 让SettlementsProxy可以访问这个方法。
        super.register(scheduler);
    }

    public SettlementsData() {
        this(new TownSet(), new FolkSet());
    }

    public static final Codec<SettlementsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TownSet.CODEC.fieldOf("towns").forGetter(d -> d.towns),
                    FolkSet.CODEC.fieldOf("folks").forGetter(d -> d.folks)
            ).apply(instance, SettlementsData::new)
    );
}
