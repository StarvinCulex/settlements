package com.outlook.hxsw.settlements.engine.schedule;

import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public interface Scheduler {
    void run(Consumer<Sidecar> sidecarTask);
    void run(ServerTask serverTask);
    void register(ServerTask serverTask);
    void unregister(ServerTask serverTask);
    int getPhase();

    interface Server extends Scheduler {
        MinecraftServer server();
    }

    interface Sidecar extends Scheduler {
        SettlementsData data();
        void register(SidecarTimer timer);
        void unregister(SidecarTimer timer);
    }
}
