package com.outlook.hxsw.settlements.folk.job.move;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.folks.job.Jobs;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;

public final class MoveTo extends AbstractMoveTo<Void> {
    public static Jobs<Void, Void> of(FolkPos origin, FolkPos destination) {
        return new MoveTo(destination).then();
    }

    MoveTo(FolkPos destination) {
        super(Codec.unit(null), destination);
    }

    public static final Codec<MoveTo> CODEC = FolkPos.CODEC.xmap(MoveTo::new, v -> v.destination);

    @Override
    public CodecType<?> getType() {
        return Type.INSTANCE;
    }

    public static final class Type implements CodecType<MoveTo> {
        public static final Type INSTANCE = new Type();

        private Type() {}

        @Override
        public String getName() {
            return "MoveTo";
        }

        @Override
        public Codec<MoveTo> codec() {
            return CODEC;
        }
    }
}
