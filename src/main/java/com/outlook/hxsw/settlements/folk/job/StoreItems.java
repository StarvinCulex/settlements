package com.outlook.hxsw.settlements.folk.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.commercial.market.Stock;
import com.outlook.hxsw.settlements.commercial.market.Stockpile;
import com.outlook.hxsw.settlements.commercial.market.WithStockpile;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.data.id.BuildingID;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.folks.job.Job;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.folk.action.ActionEmpty;

public class StoreItems extends Job<Stockpile, Void, Void> {
    private final BuildingID<Building> targetBuildingID;

    public <B extends Building & WithStockpile> StoreItems(B target) {
        this(BuildingID.of(target));
    }

    @Override
    public CodecType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public int simulationCost(Folk folk, DataScheduler scheduler, Stockpile input) {
        return 0;
    }

    @Override
    protected Void simulate(Folk folk, DataScheduler scheduler, Stockpile input) {
        var optBuilding = targetBuildingID.get(scheduler);
        if (optBuilding.isEmpty()) {
            return null;
        }
        Stock stock;
        if (optBuilding.get() instanceof WithStockpile s) {
            stock = s.getStock();
        } else {
            throw new RuntimeException();
        }
        input.takeAll(stock);
        return null;
    }

    private StoreItems(BuildingID<Building> targetBuildingID) {
        this.targetBuildingID = targetBuildingID;
    }

    public static final Codec<StoreItems> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuildingID.CODEC.fieldOf("targetBuildingID").forGetter(s -> s.targetBuildingID)
    ).apply(instance, StoreItems::new));

    @Override
    protected Codec<Stockpile> inputCodec() {
        return Stockpile.CODEC;
    }

    @Override
    protected ActionEmpty<Void> generateAction(Folk folk, Stockpile input) {
        return new ActionEmpty<>(null);
    }

    @Override
    protected Void afterAction(Folk folk, DataScheduler scheduler, Void output) {return null;}

    static final class Type implements CodecType<StoreItems> {
        public static final Type INSTANCE = new Type();

        @Override
        public String getName() {
            return "StoreItems";
        }

        @Override
        public Codec<StoreItems> codec() {
            return CODEC;
        }
    }
}
