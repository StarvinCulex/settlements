package com.outlook.hxsw.settlements.engine.folks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.WithParentAndID;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import java.util.Optional;

public final class Folk extends WithParentAndID<FolkSet> {
    public final FolkShareZone zone;

    FolkMaster<?> master = null; // 由FolkMaster初始化此字段。

    Folk(int id, FolkPos pos) {
        this(new FolkShareZone(id, pos, new FolkMeta("folk")));
    }

    Folk(FolkShareZone zone) {
        super(FolkSet.class, zone.id);
        this.zone = zone;
    }

    public Optional<FolkMaster<?>> getMaster() {
        return Optional.ofNullable(master);
    }

    public void addMaster(FolkMaster<?> master) {
        if (this.master != null) {
            throw new RuntimeException();
        }
        master.add(this);
        this.master = master;
    }

    public boolean tryRemoveMaster() {
        if (this.master == null) {
            return true;
        }
        if (this.master.tryRemove(this)) {
            this.master = null;
            return true;
        }
        return false;
    }

    public void kill() {
        tryRemoveMaster();
        getParent().remove(id);
    }

    @Override
    public void registerToDataScheduler(DataScheduler scheduler) {

    }

    public static final Codec<Folk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FolkShareZone.CODEC.fieldOf("zone").forGetter(f -> f.zone)
    ).apply(instance, Folk::new));
}
