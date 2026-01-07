# 系统架构

本项目主要源码位于 `src/main/java/com/outlook/hxsw/settlements/`，下述路径均以此为根目录。

本项目要求JDK21以上才可执行。

## 架构分层与关键子模块

### 1. 内核与线程逻辑（engine/）

- **schedule/** 调度器和逻辑线程体系# 系统架构

本项目主要源码位于 `src/main/java/com/outlook/hxsw/settlements/`，下述路径均以此为根目录。

本项目要求JDK21以上才可执行。

## 架构分层与关键子模块

### 1. 内核与线程逻辑（engine/）

- **schedule/** 调度器和逻辑线程体系
  - 主要类型：`DataScheduler`、`Task<S, R>`、`VoidTask<S>`
  - 支持逻辑、服务端线程的分流执行
  - 逻辑线程调度器是`DataScheduler`类型。
  - 所有的逻辑演算都将在逻辑线程中完成，这些逻辑演算的任务将作为`Task<DataScheduler, ?>`或`VoidTask<DataScheduler>`类型调度。
  - 逻辑演算如需要在服务端线程执行任务，可通过调度器注册`Task<ServerScheduler>`或`VoidTask<ServerScheduler>`任务。
  - 其中`ServerScheduler`类型是服务端线程的调度器。
- **data/** 核心状态/数据结构
  - 主要类型：`SettlementsData`（每存档一个）、`SettlementsProxy`（全局控制）、`SettlementsTable`（共享对象）、`Town`、`TownSet`
  - 子目录：`codecs/`（序列化Codec）、`utils/`（如ChildContainer）、`id/`（ID分配）
    - `SettlementsData`包含此mod一个存档实例的所有数据。每个存档只有一个此对象。
    - `SettlementsProxy`是此mod运行的控制器，每个服务端实例只有一个此对象。
    - `SettlementsTable`包含了服务端线程和逻辑线程可共享访问的对象。
    - `Town`就是mod功能的「城镇」，他们放在`TownSet`中，而`TownSet`是`SettlementsData`的成员。
- **buildings/** 建筑抽象
  - `Building`类的一个实例就是一个建筑。他们放在`BuildingSet`中，而`BuildingSet`是`Town`的成员。
- **grid/** 棋盘格抽象
  - `Grid`类表示一个2x2棋盘上的格子。所有的建筑都将占有一些`Grid`。
- **folks/** 市民核心逻辑（业务无关）
  - `Folk`类的一个实例就是一个市民。他们放在`FolkSet`中。而`FolkSet`是`SettlementsData`的成员。

### 2. 业务实现层

- **building/** 具体建筑物与逻辑实现
  - 所有建筑的与内核逻辑无关的具体实现都在此处。
- **commercial/** 经济系统，包括商品、市场、价格策略等
  - 所有经济系统相关的实现都在此处。
- **folk/job/** 市民职业、行为等扩展
  - 所有市民相关的与内核逻辑无关的具体实现都在此处。
- **entities/** 具体实体（folk、其他生物等）
  - 所有的实体都在此目录中。
- **blocks/** 方块系统（后续扩展，目前为空）
  - 所有的方块都在此处。
- **command/** 游戏命令接口
  - 所有的命令实现都在此处。
- **folk/job** *Job*的实现
  - 所有的*Job*实现都在此处。
- **folk/action** *FolkAction*的实现
  - 所有的*FolkAction*实现都在此处。
- **client/** 客户端相关代码

## 补充说明

- **多线程调度**：所有核心逻辑通过逻辑线程调度，不阻塞Minecraft主线程，通过Task机制与服务端线程通信。
- **核心数据与生命周期**：`SettlementsData` 单例持有一切全局状态，业务实现分离于核心逻辑。
- **模组扩展点**：建议新增业务目录或类型时，在此文档补充简述，方便团队与AI理解。

## 相关文档
- 调度器文档：`docs_zh/modules/schedule.md`
- *Folk*-*FolkEntity*关系文档：`docs_zh/modules/folk_entity_bridge.md`
    - 主要类型：`DataScheduler`、`Task<S, R>`、`VoidTask<S>`
    - 支持逻辑、服务端线程的分流执行
    - 逻辑线程调度器是`DataScheduler`类型。
    - 所有的逻辑演算都将在逻辑线程中完成，这些逻辑演算的任务将作为`Task<DataScheduler, ?>`或`VoidTask<DataScheduler>`类型调度。
    - 逻辑演算如需要在服务端线程执行任务，可通过调度器注册`Task<ServerScheduler>`或`VoidTask<ServerScheduler>`任务。
    - 其中`ServerScheduler`类型是服务端线程的调度器。
- **data/** 核心状态/数据结构
    - 主要类型：`SettlementsData`（每存档一个）、`SettlementsProxy`（全局控制）、`SettlementsTable`（共享对象）、`Town`、`TownSet`
    - 子目录：`codecs/`（序列化Codec）、`utils/`（如ChildContainer）、`id/`（ID分配）
      - `SettlementsData`包含此mod一个存档实例的所有数据。每个存档只有一个此对象。
      - `SettlementsProxy`是此mod运行的控制器，每个服务端实例只有一个此对象。
      - `SettlementsTable`包含了服务端线程和逻辑线程可共享访问的对象。
      - `Town`就是mod功能的「城镇」，他们放在`TownSet`中，而`TownSet`是`SettlementsData`的成员。
- **buildings/** 建筑抽象
    - `Building`类的一个实例就是一个建筑。他们放在`BuildingSet`中，而`BuildingSet`是`Town`的成员。
- **grid/** 棋盘格抽象
    - `Grid`类表示一个2x2棋盘上的格子。所有的建筑都将占有一些`Grid`。
- **folks/** 市民核心逻辑（业务无关）
    - `Folk`类的一个实例就是一个市民。他们放在`FolkSet`中。而`FolkSet`是`SettlementsData`的成员。

### 2. 业务实现层

- **building/** 具体建筑物与逻辑实现
    - 所有建筑的与内核逻辑无关的具体实现都在此处。
- **commercial/** 经济系统，包括商品、市场、价格策略等
    - 所有经济系统相关的实现都在此处。
- **folk/job/** 市民职业、行为等扩展
    - 所有市民相关的与内核逻辑无关的具体实现都在此处。
- **entities/** 具体实体（folk、其他生物等）
    - 所有的实体都在此目录中。
- **blocks/** 方块系统（后续扩展，目前为空）
    - 所有的方块都在此处。
- **command/** 游戏命令接口
    - 所有的命令实现都在此处。
- **folk/job** *Job*的实现
    - 所有的*Job*实现都在此处。
- **folk/action** *FolkAction*的实现
    - 所有的*FolkAction*实现都在此处。
- **client/** 客户端相关代码

## 补充说明

- **多线程调度**：所有核心逻辑通过逻辑线程调度，不阻塞Minecraft主线程，通过Task机制与服务端线程通信。
- **核心数据与生命周期**：`SettlementsData` 单例持有一切全局状态，业务实现分离于核心逻辑。
- **模组扩展点**：建议新增业务目录或类型时，在此文档补充简述，方便团队与AI理解。

## 相关文档
- 调度器文档：`docs_zh/modules/schedule.md`
- *Folk*-*FolkEntity*关系文档：`docs_zh/modules/folk_entity_bridge.md`
- 数据存储文档：`docs_zh/modules/engine_data.md`