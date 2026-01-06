package com.outlook.hxsw.settlements.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.folks.FolkSet;
import com.outlook.hxsw.settlements.engine.folks.FolkShareZone;
import com.outlook.hxsw.settlements.engine.folks.job.WithWorkGroup;
import com.outlook.hxsw.settlements.engine.folks.job.WorkGroup;
import com.outlook.hxsw.settlements.engine.folks.pos.InBuilding;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandSettlementsBuildingAddWorker extends CommandSettlementsBuilding {
    private static final String COUNT = "count";

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        registerCommand(event, Commands.literal("add").then(
                Commands.literal("worker").executes(this).then(
                        Commands.argument(COUNT, IntegerArgumentType.integer(1)).executes(this)
                )
        ));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context, Building building) throws CommandSyntaxException {
        int count;
        try {
            count = IntegerArgumentType.getInteger(context, COUNT);
        } catch (IllegalArgumentException ex) {
            count = 1;
        }
        if (!(building instanceof WithWorkGroup<?> wg)) {
            context.getSource().sendSystemMessage(Component.translatable(
                    "command.settlements.town.building.unsupported",
                    building.toShortString()
            ));
            return 1;
        }
        FolkSet folkSet = building.scheduler().data().folks;;
        WorkGroup<?> workGroup = wg.getWorkGroup();
        InBuilding pos = building.getFolkPos();
        for (int i = 0; i < count; i++) {
            Folk folk = folkSet.newFolk(pos);
            folk.addMaster(workGroup);
        }
        context.getSource().sendSystemMessage(Component.translatable(
                "command.settlements.town.building.add_folk.success"
        ));
        return 0;
    }
}
