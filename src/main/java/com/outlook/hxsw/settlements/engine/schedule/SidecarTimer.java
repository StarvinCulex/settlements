package com.outlook.hxsw.settlements.engine.schedule;

import java.util.function.Consumer;

public record SidecarTimer(int phase, int period, Consumer<Scheduler.Sidecar> task) {
    public SidecarTimer {
        if (period <= 0) {
            throw new IllegalArgumentException("SidecarTimer period <= 0");
        }
        if (task == null) {
            throw new IllegalArgumentException("SidecarTimer task is null");
        }
        phase = ((phase % period) + period) % period;
    }
}