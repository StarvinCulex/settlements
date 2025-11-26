package com.outlook.hxsw.settlements.engine.folks.job;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecElement;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.folk.job.RegisteredJobs;

public abstract class Job<I, O> implements CodecElement {
    public final Jobs<I, O> then() {
        return new Jobs<>(this);
    }

    public final <R> Jobs<I, R> then(Job<O, R> job) {
        return new Jobs<>(this, job);
    }

    public final <R> Jobs<I, R> then(Jobs<O, R> jobs) {
        return jobs.before(this);
    }

    public abstract int simulationCost(Folk folk, DataScheduler scheduler, I input);
    protected abstract O simulate(Folk folk, DataScheduler scheduler, I input);

    protected abstract Codec<I> inputCodec();

    public static final Codec<Job<?, ?>> CODEC = RegisteredJobs.INSTANCE;
}
