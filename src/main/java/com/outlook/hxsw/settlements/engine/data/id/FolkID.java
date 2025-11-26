package com.outlook.hxsw.settlements.engine.data.id;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.folks.FolkSet;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import java.util.Optional;

public final class FolkID extends ID<FolkSet, Folk> {
    private FolkID(int value) {
        super(value);
    }

    public static FolkID empty() {
        return new FolkID(0);
    }

    public static FolkID of(Folk folk) {
        return new FolkID(folk.id);
    }

    public Optional<Folk> get(SidecarData data) {
        return get(data.scheduler());
    }

    public Optional<Folk> get(DataScheduler scheduler) {
        return get(scheduler.data());
    }

    public Optional<Folk> get(SettlementsData data) {
        return get(data.folks);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FolkID fid && value == fid.value;
    }

    public static final Codec<FolkID> CODEC = Codec.INT.xmap(FolkID::new, f -> f.value);
}
