package com.outlook.hxsw.settlements.folk;

import com.outlook.hxsw.settlements.SettlementsMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ResidentEntity extends PathfinderMob {
    public ResidentEntity(EntityType<? extends ResidentEntity> type, Level level) {
        super(type, level);
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, SettlementsMain.MODID);
}