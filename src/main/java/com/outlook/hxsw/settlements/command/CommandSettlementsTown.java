package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.utils.grid.Grid;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public abstract class CommandSettlementsTown extends CommandSettlements {
    public static final String TOWN_ID = "town_id";

    public abstract int run(CommandContext<CommandSourceStack> context, Town town) throws CommandSyntaxException;

    @Override
    public int run(CommandContext<CommandSourceStack> context, SettlementsData data) throws CommandSyntaxException {
        Town town;
        try {
            int townID = IntegerArgumentType.getInteger(context, TOWN_ID);
            town = data.getTown(townID);
            if (town == null) {
                context.getSource().sendSystemMessage(Component.translatable("command.settlements.err.town_not_found", "=" + townID));
                return 1;
            }
        } catch (IllegalArgumentException ex) {
            var dimension = context.getSource().getLevel().dimension();
            var at = BlockPos.containing(context.getSource().getPosition());
            var optTown = data.getTown(dimension, new Grid(at));
            if (optTown.isEmpty()) {
                // 真坑。括号、花括号、冒号等字符不能填充。
                context.getSource().sendSystemMessage(Component.translatable("command.settlements.err.town_not_found", at.toShortString()));
                return 1;
            }
            town = optTown.get();
        }

        return run(context, town);
    }
}
