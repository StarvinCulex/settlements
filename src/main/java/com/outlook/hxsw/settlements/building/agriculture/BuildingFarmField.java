package com.outlook.hxsw.settlements.building.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.utils.filter.*;
import com.outlook.hxsw.settlements.building.utils.*;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.utils.grid.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.Supplier;

public final class BuildingFarmField extends FieldBuilding<BuildingFarmField.Type> {
    private BuildingFarmField(Type variant, NearArgument<ConnectionArgument<BuildingLocation<GridCell>>> arg) {
        super(variant, arg, variant.name);
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
                        getType().plant,
                        getType().plant.defaultBlockState().setValue(getType().plantAgeProperty, getGrowStage())
                )
        };
    }

    public static final Codec<BuildingFarmField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FieldProperty.CODEC.fieldOf("properties").forGetter(BuildingFarmField::getProperties)
    ).apply(instance, BuildingFarmField::new));

    public enum Type implements Supplier<ConnectiveBuildingPattern>, FieldBuilding.Type<BuildingFarmField> {
        WHEAT("wheat_field", Blocks.WHEAT, 7, BlockStateProperties.AGE_7),
        CARROT("carrot_field", Blocks.CARROTS, 7, BlockStateProperties.AGE_7),
        POTATO("potato_field", Blocks.POTATOES, 7, BlockStateProperties.AGE_7),
        BEETROOT("beetroot_field", Blocks.BEETROOTS, 3, BlockStateProperties.AGE_3)
        ;

        private final String name;
        private final Block plant;
        private final int growMaxStage;
        private final IntegerProperty plantAgeProperty;

        Type(String name, Block plant, int growMaxStage, IntegerProperty plantAgeProperty) {
            this.name = name;
            this.plant = plant;
            this.growMaxStage = growMaxStage;
            this.plantAgeProperty = plantAgeProperty;
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
        public BuildingFarmField make(NearArgument<ConnectionArgument<BuildingLocation<GridCell>>> args) {
            return new BuildingFarmField(this, args);
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
