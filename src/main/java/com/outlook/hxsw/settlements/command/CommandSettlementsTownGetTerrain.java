package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import com.outlook.hxsw.settlements.engine.grid.GridTerrain;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandSettlementsTownGetTerrain extends CommandSettlementsTown {
    private static final String POSITION = "position";

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        registerCommand(event, Commands.literal("get").then(
                Commands.literal("terrain").executes(this).then(
                        Commands.argument(POSITION, BlockPosArgument.blockPos())
                                .executes(this)
                )
        ));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context, Town town) throws CommandSyntaxException {
        BlockPos executeAt;
        try {
            executeAt = BlockPosArgument.getBlockPos(context, POSITION);
        } catch (IllegalArgumentException e) {
            try {
                executeAt = BlockPos.containing(context.getSource().getPosition());
            } catch (NullPointerException ex) {
                context.getSource().sendSystemMessage(Component.translatable("command.settlements.err.source_null"));
                return 1;
            }
        }

        Grid grid = new Grid(executeAt);
        GridTerrain terrain = town.buildings().getTerrainMap().get(grid);
        if (terrain == null) {
            context.getSource().sendSystemMessage(Component.translatable("command.settlements.town.terrain.null",
                    grid.toString(), town.getName(), String.valueOf(town.getID())));
            return 1;
        }

        context.getSource().sendSystemMessage(Component.literal(grid + ": " + terrain));
        return 0;
    }
}
