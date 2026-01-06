package com.outlook.hxsw.settlements.entities.folk;

import com.outlook.hxsw.settlements.SettlementsMain;
import com.outlook.hxsw.settlements.engine.folks.FolkShareZone;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public final class FolkEntityReference {
    private final FolkEntity entity;

    FolkEntityReference(FolkEntity entity) {
        this.entity = entity;
    }

    /**
     * 我们理应只通过这个函数创建这个实体。
     */
    public static Optional<FolkEntity> getOrGenerate(MinecraftServer server, FolkShareZone zone) {
        @Nullable FolkEntityReference ref = zone.entityReference.get();
        if (ref != null) {
            return Optional.of(ref.entity);
        }

        FolkPos pos = zone.pos.get();
        Level level = Objects.requireNonNull(server.getLevel(pos.dimension()));
        return pos.spawnPoint(level)
                .flatMap(at -> spawnFolk(level, at, zone.id))
                .filter(e -> new FolkEntityReference(e).tryRegisterIntoZone(zone));
    }

    public boolean tryRegisterIntoZone(FolkShareZone zone) {
        return zone.entityReference.compareAndSet(null, this);
    }

    public void unregisterFromZone(FolkShareZone zone) {
        zone.entityReference.compareAndSet(this, null);
    }

    private static Optional<FolkEntity> spawnFolk(Level world, BlockPos spawnAt, int id) {
        // 1. 创建实体实例
        FolkEntity folk = FolkEntity.FOLK.get().create(world, EntitySpawnReason.EVENT);
        if (folk == null) return Optional.empty(); // 注册或构造异常处理

        // 2. 设置坐标
        folk.setPos(spawnAt.getX() + 0.5, spawnAt.getY(), spawnAt.getZ() + 0.5);

        // 3. 设置自定义 folkID
        folk.assignFolkID(id);

        // 4. 实体加进 world
        boolean added = world.addFreshEntity(folk);

        if (added) {
            SettlementsMain.LOGGER.info(
                    "[Folk] Spawned folk entity uuid={} folkID={} dim={} pos=({}, {}, {})",
                    folk.getUUID(),
                    id,
                    world.dimension().location(),
                    spawnAt.getX(), spawnAt.getY(), spawnAt.getZ()
            );
        } else {
            SettlementsMain.LOGGER.warn(
                    "[Folk] Failed to spawn folk entity folkID={} dim={} pos=({}, {}, {})",
                    id,
                    world.dimension().location(),
                    spawnAt.getX(), spawnAt.getY(), spawnAt.getZ()
            );
        }

        return added ? Optional.of(folk) : Optional.empty();
    }
}
