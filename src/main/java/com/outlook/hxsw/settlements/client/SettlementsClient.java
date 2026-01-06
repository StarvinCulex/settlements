package com.outlook.hxsw.settlements.client;

import com.outlook.hxsw.settlements.SettlementsMain;
import com.outlook.hxsw.settlements.client.render.FolkEntityRenderer;
import com.outlook.hxsw.settlements.entities.folk.FolkEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@OnlyIn(Dist.CLIENT)
@Mod(SettlementsMain.MODID)
public final class SettlementsClient {
    public static final ModelLayerLocation FOLK_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(SettlementsMain.MODID, "folk"),
            "main"
    );

    private SettlementsClient() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FolkEntity.FOLK.get(), FolkEntityRenderer::new);
        SettlementsMain.LOGGER.info("[Client] Registered renderer for entity type: {}",
                FolkEntity.FOLK.get().builtInRegistryHolder().key().location());
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                FOLK_LAYER,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64)
        );
        SettlementsMain.LOGGER.info("[Client] Registered model layer: {}", FOLK_LAYER);
    }
}
