package com.outlook.hxsw.settlements.command;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import com.outlook.hxsw.settlements.engine.data.SettlementsProxy;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

public abstract class CommandSettlements implements Command<CommandSourceStack> {
    public static final String COMMAND_HEAD = "settlements";

    public static List<CommandSettlements> getCommands() {
        return List.of(
                new CommandSettlementsAddTown(),
                new CommandSettlementsTownAddBuilding(),
                new CommandSettlementsTownListBuilding(),
                new CommandSettlementsListTown(),
                new CommandSettlementsTownGetTerrain(),
                new CommandSettlementsBuildingGetMeta(),
                new CommandSettlementsTownTestBuild()
        );
    }

    public abstract int run(CommandContext<CommandSourceStack> context, SettlementsData data) throws CommandSyntaxException;

    @Override
    public final int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SettlementsProxy proxy = SettlementsProxy.get(context.getSource().getServer());
        try (var data = proxy.getData()){
            return run(context, data.get());
        } catch (RuntimeException e) {
            e.printStackTrace(System.out);
            throw e;
        }
    }

    protected void registerCommand(
            RegisterCommandsEvent event,
            ArgumentBuilder<CommandSourceStack, ?> argument
    ) {
        event.getDispatcher().register(Commands.literal(COMMAND_HEAD).then(argument));
    }
}
