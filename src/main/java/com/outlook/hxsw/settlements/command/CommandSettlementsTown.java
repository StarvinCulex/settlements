package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public abstract class CommandSettlementsTown extends CommandSettlements {
    public static final String TOWN_ID = "town_id";

    public abstract int run(CommandContext<CommandSourceStack> context, Town town) throws CommandSyntaxException;

    @Override
    public final int run(CommandContext<CommandSourceStack> context, SettlementsData data) throws CommandSyntaxException {
        Town town;
        try {
            int townID = IntegerArgumentType.getInteger(context, TOWN_ID);
            var opTtown = data.towns.get(townID);
            if (opTtown.isEmpty()) {
                context.getSource().sendSystemMessage(Component.translatable("command.settlements.err.town_not_found", "=" + townID));
                return 1;
            }
            town = opTtown.get();
        } catch (IllegalArgumentException ex) {
            var dimension = context.getSource().getLevel().dimension();
            var at = BlockPos.containing(context.getSource().getPosition());
            var optTown = data.towns.get(dimension, new Grid(at));
            if (optTown.isEmpty()) {
                // 真坑。括号、花括号、冒号等字符不能填充。
                context.getSource().sendSystemMessage(Component.translatable("command.settlements.err.town_not_found", at.toShortString()));
                return 1;
            }
            town = optTown.get();
        }

        return run(context, town);
    }

    @Override
    protected void registerCommand(
            RegisterCommandsEvent event,
            ArgumentBuilder<CommandSourceStack, ?> argument
    ) {
        super.registerCommand(event, Commands.literal("town").then(argument));
        super.registerCommand(event, Commands.literal("town").then(Commands.argument(TOWN_ID, IntegerArgumentType.integer()).then(argument)));
    }
}
