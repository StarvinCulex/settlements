package com.outlook.hxsw.settlements.engine.folks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.utils.ChildContainer;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;

import java.util.Collection;

public final class FolkSet extends ChildContainer<Folk, SettlementsData> {
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

    public static final Codec<FolkSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("nextChildID").forGetter(FolkSet::getNextChildID),
            Codec.list(Folk.CODEC).fieldOf("folks").forGetter(FolkSet::getChildrenList)
    ).apply(instance, FolkSet::new));
}
