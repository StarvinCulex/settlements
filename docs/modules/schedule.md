# engine/schedule Module Description

## 1. Module Overview

- This module is responsible for task scheduling on the mod’s **logic thread**, and for **event/data interaction with the main (server) thread**.

## 2. Multithreading and Concurrency Model

- Relationship between the logic thread and the main (server) thread:

    1. When the server starts, a unique instance of  
       `com.outlook.hxsw.settlements.engine.data.SettlementsProxy`  
       is created.
        - This instance can be obtained via its static method  
          `get(MinecraftServer server)`.

    2. This `SettlementsProxy` instance will start the **logic thread**.  
       The logic thread executes all incoming `Task<DataScheduler, ?>` and `VoidTask<DataScheduler>` tasks.

    3. During specific server tick events:
        - All `Task<ServerScheduler, ?>` and `VoidTask<ServerScheduler>` tasks are consumed and executed on the server thread.
        - In addition, a task is sent to the logic thread that increments the logic scheduler’s internal timer **phase** by 1, keeping the logic thread’s time progression synchronized with the server ticks.

    4. The logic thread and the main thread can send tasks to each other.
        - The execution of these tasks is **not protected by locks**, so thread safety must be handled carefully.
        - It is recommended to only send **immutable objects** between threads to avoid concurrency issues.
