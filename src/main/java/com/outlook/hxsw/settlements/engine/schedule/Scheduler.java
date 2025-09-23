package com.outlook.hxsw.settlements.engine.schedule;

import com.outlook.hxsw.settlements.engine.data.SettlementsData;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

/**
 * 调度器接口，定义了异步任务调度的统一规范。
 * 可用于服务端主线程与数据线程（原Sidecar/现Data）两类调度。
 */
public interface Scheduler {
    /**
     * 注册并让数据线程立即执行这个任务。
     */
    void run(Consumer<Scheduler.Data> dataTask);

    /**
     * 注册一个服务端线程执行的任务。如果当前服务端不满足ServerTask定义的条件，就不会执行。
     */
    void run(ServerTask serverTask);

    /**
     * 注册一个服务端线程执行的任务。任务会一直等待到服务端满足ServerTask定义的条件再执行。
     */
    void register(ServerTask serverTask);

    /**
     * 取消一个通过register注册的ServerTask。
     */
    void unregister(ServerTask serverTask);

    /**
     * 获取数据线程的周期。
     */
    int getPhase();

    /**
     * 服务端主线程调度器接口。
     */
    interface Server extends Scheduler {
        /**
         * 获取底层的MinecraftServer对象。
         */
        MinecraftServer server();
    }

    /**
     * 数据线程调度器接口。
     * 负责SettlementsData相关的调度与定时任务处理。
     */
    interface Data extends Scheduler {
        /**
         * 获取当前的数据对象。
         */
        SettlementsData data();

        /**
         * 注册数据线程定期执行的任务。
         */
        void register(SidecarTimer timer);

        /**
         * 注销通过register注册的SidecarTimer任务。
         */
        void unregister(SidecarTimer timer);
    }
}
