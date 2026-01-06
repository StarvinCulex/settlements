package com.outlook.hxsw.settlements.folk.job.move;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.commercial.market.Stockpile;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.folks.job.Job;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

public abstract class AbstractMoveTo<T> extends Job<T, T, T> {
    private final Codec<T> inputCodec;
    public final FolkPos destination;
    public final float speedFactor = 1;
    public static final float SPEED_GRID_PER_PHASE = 1;

    protected AbstractMoveTo(Codec<T> inputCodec, FolkPos destination) {
        this.inputCodec = inputCodec;
        this.destination = destination;
    }

    @Override
    protected final T simulate(Folk folk, DataScheduler scheduler, T input) {
        folk.zone.pos.lazySet(destination);
        return input;
    }

    @Override
    public int simulationCost(Folk folk, DataScheduler scheduler, T input) {
        Grid current = folk.zone.pos.get().grid();
        Grid target = destination.grid();
        float deltaU = Math.abs(target.u - current.u);
        float deltaV = Math.abs(target.v - current.v);
        float distance = deltaU + deltaV;
        float phaseCost = distance * SPEED_GRID_PER_PHASE * speedFactor;
        return Math.max(1, Math.round(phaseCost));
    }

    @Override
    protected T afterAction(Folk folk, DataScheduler scheduler, T output) {
        folk.zone.pos.lazySet(destination);
        return output;
    }

    @Override
    protected final Codec<T> inputCodec() {
        return inputCodec;
    }
}
