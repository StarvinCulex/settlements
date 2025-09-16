package com.outlook.hxsw.settlements.folk;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

public record ResidentKey(UUID id) {
    public Optional<ResidentEntity> getIfLoaded(ServerLevel level) {
        Optional<Entity> entityOpt = Optional.ofNullable(level.getEntity(id));
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }
        Entity entity = entityOpt.get();
        if (!(entity instanceof ResidentEntity resident)) {
            return Optional.empty();
        }
        if (resident.isDeadOrDying()) {
            throw new IllegalStateException("ResidentEntity is dead or dying");
        }
        return Optional.of(resident);
    }

    public static final Codec<ResidentKey> CODEC = Codec.STRING.xmap(
        s -> new ResidentKey(UUID.fromString(s)),
        rk -> rk.id().toString()
    );
}
