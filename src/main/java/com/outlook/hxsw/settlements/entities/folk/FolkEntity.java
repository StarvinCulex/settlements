package com.outlook.hxsw.settlements.entities.folk;

import com.outlook.hxsw.settlements.SettlementsMain;
import com.outlook.hxsw.settlements.engine.data.SettlementsProxy;
import com.outlook.hxsw.settlements.engine.folks.FolkShareZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FolkEntity extends PathfinderMob {
    public static final EntityDataAccessor<Integer> FOLK_ID =
            SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);

    private int folkID;
    // Server-only bookkeeping; clients only need the synced folkID for rendering/debug info.
    private @Nullable FolkShareZone zone;
    private @Nullable FolkEntityReference ref;
    @Nullable FolkActionContext<?> actionContext;

    public FolkEntity(EntityType<? extends FolkEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FolkActionGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FOLK_ID, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FOLK_ID.equals(key)) {
            this.folkID = entityData.get(FOLK_ID);
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (level().isClientSide()) {
            return;
        }

        var table = SettlementsProxy.get(level().getServer()).getTable();
        var zone = table.getFolkShareZone(folkID);
        if (zone.isEmpty()) {
            destroyExpired();
            return;
        }
        this.zone = zone.get();

        this.ref = new FolkEntityReference(this);
        if (!this.ref.tryRegisterIntoZone(this.zone)) {
            destroyExpired();
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (level().isClientSide()) {
            return;
        }

        if (actionContext != null) {
            actionContext.unloadCallback().run();
            actionContext = null;
        }
        if (this.zone == null || this.ref == null) {
            return;
        }

        this.ref.unregisterFromZone(this.zone);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide() && actionContext != null) {
            actionContext.failCallback().accept(FolkActionFailure.KILLED);
            actionContext.action().cancel();
            actionContext = null;
        }
        super.die(source);
    }

    public static final Supplier<EntityType<FolkEntity>> FOLK = SettlementsMain.ENTITIES.register(
            "folk",
            () -> EntityType.Builder.of(FolkEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
//                    .clientTrackingRange(64)
                    .noSave()
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            ResourceLocation.fromNamespaceAndPath(SettlementsMain.MODID, "folk")
                    ))
    );

    @Nullable public FolkAction<?> getCurrentAction() {
        return actionContext == null ? null : actionContext.action();
    }

    public <A> void setAction(
            FolkAction<A> action,
            Consumer<A> finishCallback,
            Runnable unloadCallback,
            Consumer<FolkActionFailure> failCallback
    ) {
        action.setFolkEntity(this);
        actionContext = new FolkActionContext<>(
                action,
                finishCallback,
                unloadCallback,
                failCallback
        );
    }

    <A> void onActionCompleted(FolkAction<A> action, A output) {
        if (level().isClientSide()) {
            return;
        }
        if (this.actionContext == null || this.actionContext.action() != action) {
            return;
        }
        @SuppressWarnings("unchecked") // 因为两个FolkAction相等，所以范型类型确认无误
        FolkActionContext<A> context = (FolkActionContext<A>) this.actionContext;

        this.actionContext = null;
        context.finishCallback().accept(output);
    }

    void assignFolkID(int id) {
        this.folkID = id;
        entityData.set(FOLK_ID, id);
    }

    private void destroyExpired() {
        remove(RemovalReason.DISCARDED);
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                FOLK.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FolkEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.AND
        );
    }

    private static boolean canSpawn(EntityType<FolkEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }
}
