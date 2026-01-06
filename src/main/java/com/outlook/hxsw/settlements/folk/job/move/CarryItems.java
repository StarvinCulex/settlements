package com.outlook.hxsw.settlements.folk.job.move;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.commercial.market.Stockpile;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.folks.job.Jobs;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.entities.folk.FolkAction;
import com.outlook.hxsw.settlements.folk.action.move.ActionCarryItems;

public final class CarryItems extends AbstractMoveTo<Stockpile> {
    public static Jobs<Stockpile, Stockpile> of(FolkPos origin, FolkPos destination) {
        return new CarryItems(destination).then();
    }

    CarryItems(FolkPos destination) {
        super(Stockpile.CODEC, destination);
    }

    public static final Codec<CarryItems> CODEC = FolkPos.CODEC.xmap(CarryItems::new, c -> c.destination);

    @Override
    public CodecType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    protected ActionCarryItems generateAction(Folk folk, Stockpile input) {
        return new ActionCarryItems(destination, input);
    }

    public static final class Type implements CodecType<CarryItems> {
        public static final CarryItems.Type INSTANCE = new Type();

        private Type() {}

        @Override
        public String getName() {
            return "CarryItems";
        }

        @Override
        public Codec<CarryItems> codec() {
            return CODEC;
        }
    }
}
