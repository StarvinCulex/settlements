package com.outlook.hxsw.settlements.building.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.utils.filter.FacingArgument;
import com.outlook.hxsw.settlements.engine.buildings.*;
import com.outlook.hxsw.settlements.engine.grid.*;
import com.outlook.hxsw.settlements.engine.schedule.ServerScheduler;
import com.outlook.hxsw.settlements.engine.schedule.Task;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;

public abstract class BasicBuilding<P extends Enum<P> & Supplier<StructurePattern>> extends PatternableBuilding<GridRegion, P> {
    protected BasicBuilding(
            int id,
            P pattern,
            FacingArgument<BuildingLocation<GridRegion>> location,
            String name
    ) {
        this(pattern.getDeclaringClass(), new BasicProperties(id, location.inner(), name, pattern.name(), location.facing()));
    }

    protected BasicBuilding(Class<P> variantClass, BasicProperties properties) {
        super(variantClass, properties);
    }

    public final GridSide getFacing() {
        return getProperties().facing;
    }

    @Override
    public int getFootingY() {
        return getGroundY() - getBuildingPattern().get().groundSurfaceY;
    }

    @Override
    public Task<ServerScheduler, Void> getBuilder() {
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

    public static class BasicProperties extends PatternableBuilding.BasicProperties<GridRegion> {
        final GridSide facing;

        private BasicProperties(PatternableBuilding.BasicProperties<GridRegion> basic, GridSide facing) {
            super(basic);
            this.facing = facing;
        }

        protected BasicProperties(BasicProperties prop) {
            this(prop, prop.facing);
        }

        public BasicProperties(int id, BuildingLocation<GridRegion> location, String name, String buildingPattern, GridSide facing) {
            this(new PatternableBuilding.BasicProperties<>(id, location, name, buildingPattern), facing);
        }

        public static Codec<BasicProperties> CODEC =  RecordCodecBuilder.create(instance ->
                instance.group(
                        PatternableBuilding.BasicProperties.codec(GridRegion.CODEC).fieldOf("basic").forGetter(p -> p),
                        GridSide.CODEC.fieldOf("facing").forGetter(p -> p.facing)
                ).apply(instance, BasicProperties::new)
        );
    }
}
