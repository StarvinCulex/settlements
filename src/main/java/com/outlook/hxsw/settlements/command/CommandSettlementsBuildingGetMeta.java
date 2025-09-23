package com.outlook.hxsw.settlements.command;

import com.google.gson.Gson;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandSettlementsBuildingGetMeta extends CommandSettlementsBuilding {
    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        registerCommand(event, Commands.literal("get").then(
                Commands.literal("meta").executes(this)
        ));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context, Building building) throws CommandSyntaxException {
        String json = intoJSON(building);
        context.getSource().sendSystemMessage(Component.literal(json));
        return 0;
    }

    private static <B extends Building> String intoJSON(B building) {
        @SuppressWarnings("unchecked")
        Codec<B> codec = (Codec<B>) building.getType().codec();
        var result = codec.encodeStart(JsonOps.INSTANCE, building);
        if (result.result().isPresent()) {
            var json = result.result().get();
            return new Gson().toJson(json);
        } else {
            return "null";
        }
    }
}
