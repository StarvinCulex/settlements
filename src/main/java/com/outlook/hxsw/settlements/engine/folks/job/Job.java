package com.outlook.hxsw.settlements.engine.folks.job;

import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecElement;
import com.outlook.hxsw.settlements.engine.folks.Folk;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;
import com.outlook.hxsw.settlements.entities.folk.FolkAction;
import com.outlook.hxsw.settlements.folk.job.RegisteredJobs;

/**
 * Job类必须是一个不可变的类，因为要多个线程共享。
 */
public abstract class Job<I, A, O> implements CodecElement {
    public final Jobs<I, O> then() {
        return new Jobs<>(this);
    }

    public final <R> Jobs<I, R> then(Job<O, ?, R> job) {
        return new Jobs<>(this, job);
    }

    public final <R> Jobs<I, R> then(Jobs<O, R> jobs) {
        return jobs.before(this);
    }

    protected abstract int simulationCost(Folk folk, DataScheduler scheduler, I input);
    protected abstract O simulate(Folk folk, DataScheduler scheduler, I input);

    protected abstract Codec<I> inputCodec();

    protected abstract FolkAction<A> generateAction(Folk folk, I input);
    protected abstract O afterAction(Folk folk, DataScheduler scheduler, A output);

    public static final Codec<Job<?, ?, ?>> CODEC = RegisteredJobs.INSTANCE;
}
