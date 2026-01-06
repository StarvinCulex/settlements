package com.outlook.hxsw.settlements.folk.action.build;

import com.outlook.hxsw.settlements.entities.folk.FolkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

class BuildStepBreakBlock implements BuildStep {
    private final BlockPos target;
    private final Predicate<BlockState> confirmation;
    private int tickRemain; // -1 说明没有初始化，需要用函数计算一下效率。

    public BuildStepBreakBlock(BlockPos target) {
        this(target, -1, x -> true);
    }

    public BuildStepBreakBlock(BlockPos target, int tickCost, Predicate<BlockState> confirmation) {
        this.target = target;
        this.tickRemain = tickCost;
        this.confirmation = confirmation;
    }

    @Override
    public void start(BasicBuildAction buildAction, FolkEntity entity) {
        BlockState state = entity.level().getBlockState(target);
        if (!confirmation.test(state)) {
            return;
        }

        if (tickRemain == -1) {
            tickRemain = calculateBreakTicks(entity, target, state);
        }
    }

    @Override
    public void tick(BasicBuildAction buildAction, FolkEntity entity) {
        if (tickRemain-- <= 0) {
            BlockState state = entity.level().getBlockState(target);
            if (confirmation.test(state)) {
                entity.level().destroyBlock(target, false, entity);
            }
            buildAction.nextStep();
        }
    }

    @Override
    public void stop(BasicBuildAction buildAction, FolkEntity entity) {}

    private static int calculateBreakTicks(FolkEntity entity, BlockPos pos, BlockState state) {
        Level level = entity.level();
        if (state.isAir()) {
            return 0;
        }

        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F) {
            return Integer.MAX_VALUE;
        }

        ItemStack tool = entity.getMainHandItem();
        boolean effective = !state.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(state);
        float ticks = hardness * (effective ? 30.0F : 100.0F);
        if (ticks <= 0.0F) {
            return 0;
        }

        return (int) Math.ceil(ticks);
    }
}
