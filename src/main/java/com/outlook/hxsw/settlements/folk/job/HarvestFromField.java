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
import com.outlook.hxsw.settlements.entities.folk.FolkAction;
import com.outlook.hxsw.settlements.folk.action.ActionEmpty;
import com.outlook.hxsw.settlements.folk.action.build.ActionHarvestFromField;
import com.outlook.hxsw.settlements.folk.job.move.CarryItems;
import com.outlook.hxsw.settlements.folk.job.move.MoveTo;

public class HarvestFromField extends Job<Void, Void, Stockpile> {
    public static Jobs<Void, Void> make(HarvesterBuilding<?> harvester, FieldBuilding<?> field) {
        return MoveTo.of(harvester.getFolkPos(), field.getFolkPos()) // 从工作站移动到田地
                .then(new HarvestFromField(field))  // 收获田地
                .then(CarryItems.of(field.getFolkPos(), harvester.getFolkPos())) // 返回到工作站
                .then(new StoreItems(harvester));  // 提交收获物
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
    protected FolkAction<Void> generateAction(Folk folk, Void input) {
        return targetBuildingID.get(folk)
                .map(ActionHarvestFromField::new)
                .map(v -> (FolkAction<Void>) v)
                .orElseGet(() -> new ActionEmpty<>(null));
    }

    @Override
    protected Stockpile afterAction(Folk folk, DataScheduler scheduler, Void ignored) {
        return simulate(folk, scheduler, null);
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
