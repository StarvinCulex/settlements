package com.outlook.hxsw.settlements.folk.action.build;

import com.outlook.hxsw.settlements.building.agriculture.FieldBuilding;
import com.outlook.hxsw.settlements.engine.folks.pos.InBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ActionHarvestFromField extends BasicBuildAction {
    private final Set<Block> harvestBlocks;
    private final InBuilding harvestPositions;
    private static final int BREAK_TICK_COST = 10;

    public ActionHarvestFromField(FieldBuilding<?> field) {
        this.harvestBlocks = field.getType().harvestBlocks();
        this.harvestPositions = field.harvestPos();
    }

    @Override
    List<BuildStep> generate() { // 效果是，走到每一个目标的附近，然后破坏它。
        List<BlockPos> targets = new ArrayList<>(harvestPositions.actionPoints(entity().level()));
        targets.sort(Comparator.<BlockPos>comparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getZ)
                .thenComparingInt(BlockPos::getY));

        List<BuildStep> steps = new ArrayList<>();
        for (BlockPos pos : targets) {
            steps.add(new BuildStepMove(pos));
            steps.add(new BuildStepBreakBlock(pos, BREAK_TICK_COST, s -> harvestBlocks.contains(s.getBlock())));
        }
        return steps;
    }
}
