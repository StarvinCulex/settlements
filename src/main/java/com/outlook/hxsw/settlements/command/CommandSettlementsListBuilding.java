package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.buildings.Buildable;
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

public class CommandSettlementsListBuilding extends CommandSettlementsTown {

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        var cmdSub = Commands.literal("list").then(
                Commands.literal("building").executes(this)
        );

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("settlements").then(cmdSub));
        dispatcher.register(Commands.literal("settlements").then(
                Commands.argument(TOWN_ID, IntegerArgumentType.integer()).then(cmdSub)
        ));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context, Town town) throws CommandSyntaxException {
        MutableComponent message = Component.translatable("command.settlements.town.buildings.head",
                town.getName(), String.valueOf(town.getID()));
        Map<BuildingType<?>, Collection<Buildable>> buildingList = new HashMap<>();
        for (Buildable b : town.buildings().getBuildings()) {
            buildingList.computeIfAbsent(b.getType(), k -> new ArrayList<>()).add(b);
        }
        for (var entry : buildingList.entrySet()) {
            message.append("\n  ").append(Component.translatable(entry.getKey().getDisplayKey())).append(": ");
            message.append(entry.getValue().stream().map(b -> b.getGrids().toString()).collect(Collectors.joining(" ")));
        }
        if (buildingList.isEmpty()) {
            message = Component.translatable("command.settlements.town.buildings.no_elements",
                    town.getName(), String.valueOf(town.getID()));
        }
        context.getSource().sendSystemMessage(message);
        return 0;
    }
}
