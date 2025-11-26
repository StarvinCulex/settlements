package com.outlook.hxsw.settlements.folk.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.building.agriculture.FieldBuilding;
import com.outlook.hxsw.settlements.building.agriculture.HarvesterBuilding;
import com.outlook.hxsw.settlements.commercial.market.Stockpile;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.data.id.BuildingID;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.folks.job.Job;
import com.outlook.hxsw.settlements.engine.folks.job.Jobs;
import com.outlook.hxsw.settlements.engine.folks.pos.InBuilding;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.folk.job.move.CarryItems;
import com.outlook.hxsw.settlements.folk.job.move.MoveTo;

public class HarvestFromField extends Job<Void, Stockpile> {
    public static Jobs<Void, Void> make(HarvesterBuilding<?> harvester, FieldBuilding<?> field) {
        return MoveTo.of(new InBuilding(harvester), new InBuilding(field))
                .then(new HarvestFromField(field))
                .then(CarryItems.of(new InBuilding(field), new InBuilding(harvester)))
                .then(new StoreItems(harvester));
    }

    private final BuildingID<FieldBuilding<?>> targetBuildingID;

    public HarvestFromField(FieldBuilding<?> target) {
        this(BuildingID.of(target));
    }

    @Override
    public int simulationCost(Folk folk, DataScheduler scheduler, Void input) {
        return targetBuildingID.get(scheduler)
                .map(FieldBuilding::getType)
                .map(FieldBuilding.Type::harvestPhaseCost)
                .orElse(0);
    }

    @Override
    protected Stockpile simulate(Folk folk, DataScheduler scheduler, Void ignored) {
        return targetBuildingID.get(scheduler)
                .map(FieldBuilding::harvest)
                .orElseGet(Stockpile::new);
    }

    @Override
    protected Codec<Void> inputCodec() {
        return Codec.unit(null);
    }

    @Override
    public CodecType<?> getType() {
        return Type.INSTANCE;
    }

    private HarvestFromField(BuildingID<FieldBuilding<?>> targetBuildingID) {
        this.targetBuildingID = targetBuildingID;
    }

    public static final Codec<HarvestFromField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuildingID.CODEC.fieldOf("targetBuildingID").forGetter(h -> h.targetBuildingID)
    ).apply(instance, HarvestFromField::new));

    static final class Type implements CodecType<HarvestFromField> {
        public static final Type INSTANCE = new Type();

        @Override
        public String getName() {
            return "HarvestFromField";
        }

        @Override
        public Codec<HarvestFromField> codec() {
            return CODEC;
        }
    }
}
