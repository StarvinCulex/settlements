package com.outlook.hxsw.settlements.engine.folks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.utils.ChildContainer;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;
import org.checkerframework.checker.units.qual.C;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public final class FolkSet extends ChildContainer<Folk, SettlementsData> {
    public final ConcurrentHashMap<Integer, FolkShareZone> zoneTable = new ConcurrentHashMap<>();

    public FolkSet() {
        super(SettlementsData.class);
    }

    public FolkSet(int nextChildID, Collection<Folk> folks) {
        super(SettlementsData.class, nextChildID, folks);
    }

    public Folk newFolk(FolkPos pos) {
        Folk folk = new Folk(generateChildID(), pos);
        add(folk);
        return folk;
    }

    @Override
    protected void add(Folk folk) {
        super.add(folk);
        zoneTable.put(folk.id, folk.zone);
    }

    @Override
    protected void remove(int id) {
        super.remove(id);
        zoneTable.remove(id);
    }

    public static final Codec<FolkSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("nextChildID").forGetter(FolkSet::getNextChildID),
            Codec.list(Folk.CODEC).fieldOf("folks").forGetter(FolkSet::getChildrenList)
    ).apply(instance, FolkSet::new));
}
