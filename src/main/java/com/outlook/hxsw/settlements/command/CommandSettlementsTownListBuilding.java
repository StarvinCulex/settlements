package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.buildings.BuildingType;
import com.outlook.hxsw.settlements.engine.data.Town;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandSettlementsTownListBuilding extends CommandSettlementsTown {

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        registerCommand(event, Commands.literal("list").then(
                Commands.literal("building").executes(this)
        ));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context, Town town) throws CommandSyntaxException {
        MutableComponent message = Component.translatable("command.settlements.town.buildings.head",
                town.getName(), String.valueOf(town.getID()));
        Map<BuildingType<?>, Collection<Building>> buildingList = new HashMap<>();
        for (Building b : town.buildings().getChildrenList()) {
            buildingList.computeIfAbsent(b.getType(), k -> new ArrayList<>()).add(b);
        }
        for (var entry : buildingList.entrySet()) {
            message.append("\n  ").append(Component.translatable(entry.getKey().getDisplayKey())).append(": ");
            message.append(entry.getValue().stream().map(Building::toShortString).collect(Collectors.joining(" ")));
        }
        if (buildingList.isEmpty()) {
            message = Component.translatable("command.settlements.town.buildings.no_elements",
                    town.getName(), String.valueOf(town.getID()));
        }
        context.getSource().sendSystemMessage(message);
        return 0;
    }
}
