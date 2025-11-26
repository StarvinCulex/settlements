package com.outlook.hxsw.settlements.engine.folks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FolkMeta(
        String name
) {
    static Codec<FolkMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(FolkMeta::name)
    ).apply(instance, FolkMeta::new));
}
