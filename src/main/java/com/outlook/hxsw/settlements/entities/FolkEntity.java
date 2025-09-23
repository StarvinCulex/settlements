package com.outlook.hxsw.settlements.entities;

import com.outlook.hxsw.settlements.SettlementsMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FolkEntity extends PathfinderMob {
    public FolkEntity(EntityType<? extends FolkEntity> type, Level level) {
        super(type, level);
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, SettlementsMain.MODID);
}