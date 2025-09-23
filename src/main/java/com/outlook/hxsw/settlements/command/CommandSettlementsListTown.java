package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

public class CommandSettlementsListTown extends CommandSettlements {
    public static final Command<CommandSourceStack> INSTANCE = new CommandSettlementsListTown();

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        registerCommand(event, Commands.literal("list").then(
                Commands.literal("towns").executes(INSTANCE)
        ));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> commandContext, SettlementsData data) throws CommandSyntaxException {
        Collection<Town> towns = data.towns.getChildrenList();
        MutableComponent message;
        if (towns.isEmpty()) {
            message = Component.translatable("command.settlements.list_town.no_elements");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Town t : towns) {
                sb.append("\n  %s".formatted(t));
            }
            message = Component.translatable("command.settlements.list_town.success", String.valueOf(towns.size()), sb.toString());
        }

        commandContext.getSource().sendSystemMessage(message);
        return 0;
    }
}
