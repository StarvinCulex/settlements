package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.outlook.hxsw.settlements.engine.data.Town;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandSettlementsAddTown extends CommandSettlements {
    private static final String ARG_NAME = "name";
    private static final String ARG_X = "x";
    private static final String ARG_Z = "z";

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralCommandNode<CommandSourceStack> cmd = dispatcher.register(
                Commands.literal("settlements").then(
                        Commands.literal("add")
                                .requires(src -> !src.isPlayer() || src.hasPermission(4))
                                .then(
                                        Commands.literal("town").then(
                                            Commands.argument(ARG_NAME, StringArgumentType.word())
                                            .executes(this)
                                            .then(
                                                Commands.argument(ARG_X, IntegerArgumentType.integer())
                                                    .then(
                                                        Commands.argument(ARG_Z, IntegerArgumentType.integer())
                                                            .executes(this)
                                                    )
                                            )
                                        )
                                )
                )
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context, SettlementsData data) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, ARG_NAME);
        int x, z;
        try {
            x = IntegerArgumentType.getInteger(context, ARG_X);
            z = IntegerArgumentType.getInteger(context, ARG_Z);
        } catch (IllegalArgumentException e) {
            var pos = context.getSource().getPosition();
            x = (int) Math.floor(pos.x);
            z = (int) Math.floor(pos.z);
        }
        data.addTown(new Town(data.generateTownID(), name, new Vec3i(x, 0, z), context.getSource().getLevel().dimension()));
        context.getSource().sendSystemMessage(Component.translatable("command.settlements.add_town.success", name, x, z));
        return 0;
    }
}
