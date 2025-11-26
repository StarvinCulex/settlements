package com.outlook.hxsw.settlements.engine.folks.job;

import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

interface WorkCallback {
    void onFinish(DataScheduler scheduler, Work work);

    WorkCallback EMPTY = new WorkCallback() {
        @Override
        public void onFinish(DataScheduler scheduler, Work work) {}
    };
}
