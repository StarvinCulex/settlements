package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.grid.*;
import com.outlook.hxsw.settlements.engine.data.Town;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.stream.Collectors;

/**
 * 这个命令用于放置建筑。
 * 这个命令有一个bug。不好修复。放置的建筑如果在当前tick注册了定时任务，那么它在当前dataPhase还会执行。
 * 这是因为定时任务在当前dataPhase不执行的机理其实是：执行当前phase定时任务时添加的定时任务不会在遍历列表中，而其他插入的任务会在定时任务执行之后。
 */
public class CommandSettlementsTownAddBuilding extends CommandSettlementsTown {
    private static final String BUILDING_TYPE = "building_type";
    private static final String POSITION = "position";

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        registerCommand(event,
                Commands.literal("add")
                        .requires(src -> src.hasPermission(4))
                        .then(
                                Commands.literal("building").then(
                                        Commands.argument(BUILDING_TYPE, StringArgumentType.word())
                                                .executes(this)
                                                .then(
                                                        Commands.argument(POSITION, BlockPosArgument.blockPos())
                                                                .executes(this)
                                                )
                                )
                        )
        );
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
        BuildingType<?> buildingType = BuildingType.getType(StringArgumentType.getString(context, BUILDING_TYPE));
        if (buildingType == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(
                    BuildingType.getTypes().stream().map(Object::toString).collect(Collectors.joining(","))
            );
        }

        boolean failed = town.buildings().makeAndPlace(buildingType.getFactory(), new Grid(executeAt)).isEmpty();
        if (failed) {
            context.getSource().sendSystemMessage(Component.translatable("command.settlements.town.build.failed_set"));
            return 1;
        }

        context.getSource().sendSystemMessage(Component.translatable("command.settlements.town.build.success"));
        return 0;
    }
}
