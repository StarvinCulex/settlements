package com.outlook.hxsw.settlements;

import com.mojang.logging.LogUtils;
import com.outlook.hxsw.settlements.building.RegisteredBuildings;
import com.outlook.hxsw.settlements.command.CommandSettlements;
import com.outlook.hxsw.settlements.engine.data.*;
import com.outlook.hxsw.settlements.entities.FolkEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(SettlementsMain.MODID)
public class SettlementsMain {
    public static final String MODID = "settlements";
    public static final int SIDECAR_TICK_RATE = 20;

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MODID);

    public SettlementsMain(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        FolkEntity.ENTITY_TYPES.register(modEventBus);

        CommandSettlements.getCommands().forEach(NeoForge.EVENT_BUS::register);
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        SettlementsProxy.get(event.getServer()).startDataSide();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        SettlementsProxy proxy = SettlementsProxy.get(event.getServer());
        proxy.consumeServerTasks(event.getServer());

        int currentTick = event.getServer().getTickCount();
        if (currentTick % SIDECAR_TICK_RATE == 0) {
            proxy.dataSideTick();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        SettlementsProxy.get(event.getServer()).stopAndSave();
    }

    @SubscribeEvent
    public void onLevelSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.dimension() != ServerLevel.OVERWORLD) {
            return;
        }
        SettlementsProxy proxy = SettlementsProxy.get(level.getServer());
        proxy.save();
    }
}
