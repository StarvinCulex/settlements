# System Architecture

This document describes the high-level architecture and directory structure of the Settlements mod.

It is intended for:
- Human developers working on this project.
- AI coding assistants as background context when generating or modifying code.

The main source code of this project is located at  
`src/main/java/com/outlook/hxsw/settlements/`.  
All paths mentioned below are relative to this root directory.

This project requires **JDK 21 or above** to run.

## Layered Architecture and Key Submodules

The codebase is roughly split into:
- **Engine / core layer** (`engine/` and subpackages): thread scheduling, core data, generic building/folk abstractions.
- **Business / gameplay layer** (`building/`, `commercial/`, `folk/job/`, etc.): domain-specific logic built on top of the engine.

Below is a detailed breakdown by package.

### 1. Core & Thread Logic (`engine/`)

- **`schedule/` — Schedulers and logical thread system**
    - Key types: `DataScheduler`, `Task<S, R>`, `VoidTask<S>`
    - Supports split execution between the logic thread and the server thread.
    - The logic-thread scheduler is `DataScheduler`.
    - All logic computations are executed on the logic thread, scheduled as `Task<DataScheduler, ?>` or `VoidTask<DataScheduler>`.
    - If logic needs to execute work on the server thread, it can register `Task<ServerScheduler>` or `VoidTask<ServerScheduler>` tasks via the scheduler.
    - `ServerScheduler` is the scheduler representing the server thread.

- **`data/` — Core state and data structures**
    - Key types: `SettlementsData` (one instance per world/save), `SettlementsProxy` (global controller), `SettlementsTable` (shared objects), `Town`, `TownSet`
    - Subdirectories:
        - `codecs/` — serialization codecs
        - `utils/` — utilities (e.g. `ChildContainer`)
        - `id/` — ID allocation
    - Notes:
        - `SettlementsData` contains all data for one mod instance in a given save. Each save has exactly one `SettlementsData`.
        - `SettlementsProxy` is the runtime controller for the mod. Each server instance has exactly one `SettlementsProxy`.
        - `SettlementsTable` contains objects that can be shared and accessed by both the server thread and the logic thread.
        - `Town` represents an in‑mod “town”. Towns are stored in `TownSet`, which is a member of `SettlementsData`.

- **`buildings/` — Building abstraction (core)**
    - Each instance of `Building` represents one building.
    - Buildings are stored in a `BuildingSet`, which is a member of `Town`.

- **`grid/` — Grid abstraction**
    - `Grid` represents a cell on a 2×2 grid.
    - All buildings occupy one or more `Grid` cells.

- **`folks/` — Core folk (citizen) logic, business‑agnostic**
    - Each instance of `Folk` represents a citizen.
    - Folks are stored in a `FolkSet`, which is a member of `SettlementsData`.

### 2. Business / Gameplay Implementation Layer

- **`building/` — Concrete buildings and their logic**
    - Contains all building-related implementations that are not part of the core engine logic.

- **`commercial/` — Economy system**
    - Includes goods, markets, pricing strategies, and all other economy-related implementations.

- **`folk/job/` — Folk professions and behaviors**
    - Contains all folk-related gameplay logic that is not part of the engine core (e.g. jobs, behaviors, extensions).

- **`entities/` — Concrete entity implementations**
    - Contains all entity types, including `folk` and other creatures.

- **`blocks/` — Block system (to be extended)**
    - All custom blocks will be implemented here (currently empty).

- **`command/` — Game command interface**
    - Contains all game command implementations.

- **`folk/action/` — Implementations of `FolkAction`**
    - Contains all `FolkAction` implementations.

- **`client/` — Client-side code**
    - Contains all client-related code.

## Additional Notes

- **Multithreaded scheduling**  
  All core logic is executed on a dedicated logic thread and does not block the Minecraft main thread.  
  Communication with the server thread is done via the `Task` mechanism.

- **Core data & lifecycle**  
  A single `SettlementsData` instance holds all global mod state for a given save.  
  Gameplay / business implementations are separated from core engine logic.

- **Mod extension points**  
  When adding new business modules or types, it is recommended to update this document with a brief description, so that both team members and AI tools can better understand the architecture.

## Related Documentation

- Scheduler documentation: `docs/modules/schedule.md`
- *Folk* and *FolkEntity* documentation: `docs/modules/folk_entity_bridge.md`
- Data Storage documentation: `docs/modules/engine_data.md`