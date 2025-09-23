package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public abstract class CommandSettlementsBuilding extends CommandSettlementsTown {
    public static final String BUILDING_ID = "building_id";

    public abstract int run(CommandContext<CommandSourceStack> context, Building building) throws CommandSyntaxException;

    @Override
    public final int run(CommandContext<CommandSourceStack> context, Town town) throws CommandSyntaxException {
        Building building;
        try {
            int buildingID = IntegerArgumentType.getInteger(context, BUILDING_ID);
            var optBuilding = town.buildings().get(buildingID);
            if (optBuilding.isEmpty()) {
                context.getSource().sendSystemMessage(Component.translatable(
                        "command.settlements.town.building.null",
                        "=" + buildingID,
                        town
                ));
                return 1;
            }
            building = optBuilding.get();
        } catch (IllegalArgumentException ex) {
            var at = BlockPos.containing(context.getSource().getPosition());
            building = town.buildings().getGridMap().get(new Grid(at));
            if (building == null) {
                context.getSource().sendSystemMessage(Component.translatable(
                        "command.settlements.town.building.null",
                        at.toShortString(),
                        town
                ));
                return 1;
            }
        }
        return run(context, building);
    }

    @Override
    protected void registerCommand(
            RegisterCommandsEvent event,
            ArgumentBuilder<CommandSourceStack, ?> argument
    ) {
        super.registerCommand(event, Commands.literal("building").then(argument));
        super.registerCommand(event, Commands.literal("building").then(Commands.argument(BUILDING_ID, IntegerArgumentType.integer()).then(argument)));
    }
}
