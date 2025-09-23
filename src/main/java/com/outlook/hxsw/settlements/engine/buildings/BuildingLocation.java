package com.outlook.hxsw.settlements.engine.buildings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.grid.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record BuildingLocation<G extends Grids> (
        ResourceKey<Level> dimension,
        G grids,
        int groundY
) implements BuildingArgument {
    @Override
    public BuildingLocation<G> location() {
        return this;
    }

    @Override
    public String toString() {
        return String.format("dim=%s,g=%s,y=%d", dimension.location(), grids, groundY);
    }

    public static <G extends Grids> Codec<BuildingLocation<G>> codec(Codec<G> gCodec) {
        return RecordCodecBuilder.create(instance ->
                instance.group(
                        Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(BuildingLocation::dimension),
                        gCodec.fieldOf("grids").forGetter(BuildingLocation::grids),
                        Codec.INT.fieldOf("groundY").forGetter(BuildingLocation::groundY)
                ).apply(instance, BuildingLocation::new)
        );
    }
}
