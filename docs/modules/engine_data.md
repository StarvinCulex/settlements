# engine/data Explanation

## Background
Within the module logic thread, there are relationships between entities that form a tree structure, with *SettlementsData* as the root. *SettlementsProxy* manages the *SettlementsData*.
```mermaid
---
title: Data Ownership Diagram
---
classDiagram
    SettlementsProxy *-- "1" SettlementsData
    SettlementsData *-- "1" TownSet
    SettlementsData *-- "1" FolkSet
    
    TownSet *-- "0..*" Town
    Town *-- "1" BuildingSet
    BuildingSet *-- "0..*" Building

    class FolkMaster {
        <<abstract>>
    }
    FolkMaster o-- "0..*" Folk: Logical Ownership

    FolkSet *-- "0..*" Folk: Storage Ownership

    WorkGroup --|> FolkMaster
    WorkGroup "1" --* WithWorkGroup: WorkGroup is indirectly under SettlementsData through certain Buildings.
    class WithWorkGroup {
        <<interface>>
        + getWorkGroup() WorkGroup
    }

    Building <|-- SomeBuilding
    WithWorkGroup <|.. SomeBuilding
```

1. Each entity needs access to the entity it belongs to.
2. Each entity must be saved and read via `com.mojang.serialization.Codec`. The assembly of ownership relationships must be mounted after the entity is fully constructed.
3. After *SettlementsData* is mounted to *SettlementsProxy*, its descendant entities have tasks to register with *DataScheduler* (which belongs to *SettlementsProxy*).

Due to these requirements, the library provides a series of tool abstract classes based on *SidecarData*. Entities in the logic thread only need to extend these tool classes to manage the entire process.

All one-to-one containment relationships are established by adding the `@Child` annotation to attribute fields.
- This attribute must be `final`. The child object must be constructed when the parent object is completed.
- This attribute must extend *WithParent*.
- The reference to the parent node in this attribute will be set after the parent object is registered. Then the tasks of this attribute are registered.

All one-to-many containment relationships are accomplished by inheriting *ChildContainer*.
- All child objects can be added dynamically via the `add` method. Note that this method is `protected`, so how child objects are added should be logically encapsulated specifically.
- Child objects must extend *WithParentAndID*.
- Registration tasks for child objects will be completed after setting the parent node reference:
    - After initialization and before parent object registration, the reference of the child object will be set after the parent is registered.
    - After the parent is registered, the reference of the child will be immediately set after the `add` method is called.

```mermaid
---
title: Data Inheritance Diagram
---
classDiagram
  class SidecarData {
    <<abstract>>
    - dataScheduler: DataScheduler
    + scheduler() DataScheduler *
    # onRegistering(DataScheduler)
  }

  SidecarData <|-- ManualSidecarData
  class ManualSidecarData {
    <<abstract>>
    # register(DataScheduler)
  }

  ManualSidecarData <|-- SettlementsData

  SidecarData <|-- WithParent
  class WithParent {
    <<abstract>>
    - parent: P
    + scheduler() DataScheduler
    + getParent() P
  }

  WithParent <|-- WithParentAndID
  class WithParentAndID {
    <<abstract>>
    + id: int
    # onRemove() *
  }

  WithParent <|-- ChildContainer
  class ChildContainer {
    <<abstract>>
    - nextChildID: int
    - children: Map< Integer, C >
    + get(int id) Optional< C >
    + getChildrenList() List< C >
    + getChildren() Map< Integer, C >
    # getNextChildID() int
    # generateChildID() int
    # add(C child)
    # remove(int id)
  }

  WithParent <|-- FolkMaster
  ChildContainer <|-- TownSet
  class TownSet {
    + add(Town town)
  }
  ChildContainer <|-- BuildingSet
  class BuildingSet {
    + makeAndPlace(...) Optional< Building >
  }
  ChildContainer <|-- FolkSet
  class FolkSet {
    + newFolk(FolkPos pos) Folk
  }

  WithParentAndID <|-- Town
  WithParentAndID <|-- Building
  WithParentAndID <|-- Folk
```
