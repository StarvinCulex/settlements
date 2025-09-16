package com.outlook.hxsw.settlements.building.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface PatternOption {
    void apply(ServerLevel level, StructureTemplate template, BlockPos origin, StructurePlaceSettings settings);

    record BlockReplacing(Block selectedBlock, Function<List<BlockState>, List<BlockState>> rule) implements PatternOption {
        @Override
        public void apply(ServerLevel level, StructureTemplate template, BlockPos origin, StructurePlaceSettings settings) {
            var templateBlockInfo = template.filterBlocks(origin, settings,selectedBlock);
            List<BlockState> states = rule.apply(templateBlockInfo.stream()
                    .map(StructureTemplate.StructureBlockInfo::state).toList());
            for (int i = 0; i < states.size(); i++) {
                var info = templateBlockInfo.get(i);
                if (info != null && info.nbt() == null) {
                    level.setBlock(info.pos(), states.get(i), StructurePattern.UPDATE_FLAG);
                }
            }
        }

        public static BlockReplacing fill(Block selectedBlock, BlockState blockState) {
            return new BlockReplacing(selectedBlock, states -> states.stream().map(x -> blockState).toList());
        }

        public static BlockReplacing showByRatio(Block selectedBlock, float ratio) {
            return new BlockReplacing(selectedBlock, states -> {
                List<BlockState> newStates = new ArrayList<>(states.size());
                int showTill = Math.round(states.size() * ratio);
                showTill = Math.max(Math.min(states.size(), showTill), 0);
                for (int i = 0; i < showTill; i++) {
                    newStates.add(states.get(i));
                }
                for (int i = showTill; i < states.size(); i++) {
                    newStates.add(Blocks.AIR.defaultBlockState());
                }
                return newStates;
            });
        }
    }
}
