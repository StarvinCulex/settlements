package com.outlook.hxsw.settlements.engine.folks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;
import com.outlook.hxsw.settlements.entities.folk.FolkEntityReference;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 这部分内存在Server Thread和Data Thread共享。
 * 这个类只由Folk类和FolkEntity使用。其他地方对此不感知。
 * 在编码上，所有default作用域的是给Data Thread用的。所有public的方法是给Server Thread用的。
 */
public final class FolkShareZone {
    public final int id;
    public final AtomicReference<FolkPos> pos;
    final AtomicReference<FolkMeta> meta;
    public final AtomicReference<FolkEntityReference> entityReference; // maybe null

    FolkShareZone(int id, FolkPos pos, FolkMeta meta) {
        this.id = id;
        this.pos = new AtomicReference<>(pos);
        this.meta = new AtomicReference<>(meta);
        this.entityReference = new AtomicReference<>();
    }

    public FolkMeta getMeta() {
        return meta.get();
    }

    static final Codec<FolkShareZone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(z -> z.id),
            FolkPos.CODEC.fieldOf("pos").forGetter(z -> z.pos.get()),
            FolkMeta.CODEC.fieldOf("meta").forGetter(z -> z.meta.get())
    ).apply(instance, FolkShareZone::new));
}
