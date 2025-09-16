package com.outlook.hxsw.settlements.building.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.utils.filter.FacingArgument;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.schedule.Scheduler;
import com.outlook.hxsw.settlements.utils.grid.*;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BasicBuilding<P extends Enum<P> & Supplier<StructurePattern>> extends Building<GridRegion, P> {
    protected BasicBuilding(
            P pattern,
            FacingArgument<BuildingLocation<GridRegion>> location,
            String name
    ) {
        this(pattern.getDeclaringClass(), new BasicProperties(location.inner(), name, pattern.name(), location.facing()));
    }

    protected BasicBuilding(Class<P> variantClass, BasicProperties properties) {
        super(variantClass, properties);
    }

    public final GridSide getFacing() {
        return getProperties().facing;
    }

    @Override
    public Consumer<Scheduler.Server> getBuilder() {
        return getBuildingPattern().get().build(
                getDimension(),
                new BlockPos(getGrids().begin().getFromX(), getGroundY(), getGrids().begin().getFromZ()),
                StructurePattern.getRotation(getFacing())
        );
    }

    @Override
    protected BasicProperties getProperties() {
        return (BasicProperties) super.getProperties();
    }

    public static class BasicProperties extends Building.BasicProperties<GridRegion> {
        final GridSide facing;

        private BasicProperties(Building.BasicProperties<GridRegion> basic, GridSide facing) {
            super(basic);
            this.facing = facing;
        }

        protected BasicProperties(BasicProperties prop) {
            this(prop, prop.facing);
        }

        public BasicProperties(BuildingLocation<GridRegion> location, String name, String buildingPattern, GridSide facing) {
            this(new Building.BasicProperties<>(location, name, buildingPattern), facing);
        }

        public static Codec<BasicProperties> CODEC =  RecordCodecBuilder.create(instance ->
                instance.group(
                        Building.BasicProperties.codec(GridRegion.CODEC).fieldOf("basic").forGetter(p -> p),
                        GridSide.CODEC.fieldOf("facing").forGetter(p -> p.facing)
                ).apply(instance, BasicProperties::new)
        );
    }
}
