package com.outlook.hxsw.settlements.building.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.utils.filter.*;
import com.outlook.hxsw.settlements.building.utils.*;
import com.outlook.hxsw.settlements.commercial.goods.GoodsItem;
import com.outlook.hxsw.settlements.commercial.goods.Sellable;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.grid.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Map;
import java.util.function.Supplier;

public final class BuildingFarmField extends FieldBuilding<BuildingFarmField.Type> {
    static final Block PLANT_TEMPLATE = Blocks.WHEAT;

    private BuildingFarmField(int id, Type variant, NearArgument<ConnectionArgument<BuildingLocation<GridCell>>> arg) {
        super(id, variant, arg, variant.name);
    }

    private BuildingFarmField(FieldProperty properties) {
        super(Type.class, properties);
    }

    @Override
    public Type getType() {
        return getBuildingPattern();
    }

    @Override
    protected PatternOption[] patternOptions() {
        return new PatternOption[]{
                PatternOption.BlockReplacing.fill(
                        PLANT_TEMPLATE,
                        getType().plant.defaultBlockState().setValue(getType().plantAgeProperty, getGrowStage())
                )
        };
    }

    public static final Codec<BuildingFarmField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FieldProperty.CODEC.fieldOf("properties").forGetter(BuildingFarmField::getProperties)
    ).apply(instance, BuildingFarmField::new));

    public enum Type implements Supplier<ConnectiveBuildingPattern>, FieldBuilding.Type<BuildingFarmField> {
        WHEAT("wheat_field", Blocks.WHEAT, 7, BlockStateProperties.AGE_7, Map.of(
                GoodsItem.of(Items.WHEAT), 64,
                GoodsItem.of(Items.WHEAT_SEEDS), 32
        )),
        CARROT("carrot_field", Blocks.CARROTS, 7, BlockStateProperties.AGE_7, Map.of(
                GoodsItem.of(Items.CARROT), 128
        )),
        POTATO("potato_field", Blocks.POTATOES, 7, BlockStateProperties.AGE_7, Map.of(
                GoodsItem.of(Items.POTATO), 128,
                GoodsItem.of(Items.POISONOUS_POTATO), 1
        )),
        BEETROOT("beetroot_field", Blocks.BEETROOTS, 3, BlockStateProperties.AGE_3, Map.of(
                GoodsItem.of(Items.BEETROOT), 64,
                GoodsItem.of(Items.BEETROOT_SEEDS), 32
        ))
        ;

        private final String name;
        private final Block plant;
        private final int growMaxStage;
        private final IntegerProperty plantAgeProperty;
        private final Map<Sellable, Integer> harvest;

        Type(String name, Block plant, int growMaxStage, IntegerProperty plantAgeProperty, Map<Sellable, Integer> harvest) {
            this.name = name;
            this.plant = plant;
            this.growMaxStage = growMaxStage;
            this.plantAgeProperty = plantAgeProperty;
            this.harvest = harvest;
        }

        @Override
        public int growTickCost() {
            return 10; // for testing. should be 900
        }

        @Override
        public int growMaxStage() {
            return growMaxStage;
        }

        @Override
        public int masterDistance() {
            return BuildingFarmstead.MASTERING_DISTANCE;
        }

        @Override
        public Map<Sellable, Integer> harvest() {
            return harvest;
        }

        @Override
        public int harvestPhaseCost() {
            return 10;
        }

        @Override
        public BuildingFarmField make(int id, NearArgument<ConnectionArgument<BuildingLocation<GridCell>>> args) {
            return new BuildingFarmField(id, this, args);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayKey() {
            return "display.settlements.building.name.farm_field";
        }

        @Override
        public Codec<BuildingFarmField> codec() {
            return CODEC;
        }

        private static final ConnectiveBuildingPattern BUILDING_PATTERN =
                new ConnectiveBuildingPattern("farmfield/basic", 0, 1);
        @Override
        public ConnectiveBuildingPattern get() {
            return BUILDING_PATTERN;
        }

        @Override
        public int connectingHeightMin() {
            return -1;
        }

        @Override
        public int connectingHeightMax() {
            return 1;
        }
    }
}
