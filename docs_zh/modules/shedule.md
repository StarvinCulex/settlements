# engine/schedule 模块说明

## 一、模块功能概述
- 本模块用于协调此Mod逻辑线程下的任务调度及与主线程的事件/数据交互。

## 二、多线程与并发模型说明
- 逻辑线程与主线程之间的关系
    1. 在服务端启动时，会生成一个唯一的`com.outlook.hxsw.settlements.engine.data.SettlementsProxy`实例。
        - 这个实例可通过它的静态方法`get(MinecraftServer server)`获取。
    2. 这个实例将会启动逻辑线程。这个逻辑线程会运行所有到来的`Task<DataScheduler, ?>`和`VoidTask<DataScheduler>`任务。
    3. 在特定的服务端tick事件中：
        - 将会消费所有的`Task<ServerScheduler, ?>`和`VoidTask<ServerScheduler>`任务。
        - 此外还会给逻辑线程发送一个让其计时器*phase*加1的任务，以此让服务器tick与逻辑线程的时间流速同步。
    4. 逻辑线程和主线程可以互相发送任务。发送的任务执行过程中无锁保护，应注意线程安全问题，只建议发送不可变的对象。
