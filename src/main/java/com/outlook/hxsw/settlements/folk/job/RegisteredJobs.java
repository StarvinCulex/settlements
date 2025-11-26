package com.outlook.hxsw.settlements.folk.job;

import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.data.codecs.DynamicCodecer;
import com.outlook.hxsw.settlements.engine.folks.job.Job;
import com.outlook.hxsw.settlements.folk.job.move.CarryItems;
import com.outlook.hxsw.settlements.folk.job.move.MoveTo;

public final class RegisteredJobs extends DynamicCodecer<Job<?, ?>, CodecType<? extends Job<?, ?>>> {
    public static final RegisteredJobs INSTANCE = new RegisteredJobs();

    private RegisteredJobs() {
        register(CarryItems.Type.INSTANCE);
        register(MoveTo.Type.INSTANCE);
        register(StoreItems.Type.INSTANCE);
        register(HarvestFromField.Type.INSTANCE);
    }
}
