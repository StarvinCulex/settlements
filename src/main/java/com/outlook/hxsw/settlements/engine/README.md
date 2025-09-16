# Settlements Engine 设计总览

本模块为定居点构筑玩法提供核心抽象与分层，结构如图、说明如下。

---

## 1. 数据变更与父子关系

模块采用监听器与父指针实现链式数据管理。
- `ChangeListener` 定义变更监听接口。
- `ChangableData` 为所有数据对象统一的变更通知基类。
- `WithParent` 提供父级引用体系，实现多级数据层次。

结构图如下：

```mermaid
classDiagram
    class ChangeListener {
        <<interface>>
        notifyChange()
    }

    class WithParent~P~ {
        <<abstract>>
        -@Nullable P parent
        +getParent() @Nullable P
        +setParent(P)
    }

    ChangeListener <|.. ChangableData~P~
    WithParent~P~ <|-- ChangableData~P~
    class ChangableData~P~ {
        <<abstract>>
    }

```


## 2. 定居点主干（核心数据、代理）

`SettlementsData` 统一维护所有城镇（`Town`）、建筑集合（`BuildingSet`），并通过 `SettlementsProxy` 协调世界内状态。

结构图如下：

```mermaid
classDiagram
    SettlementsProxy <-- SettlementsData
    Scheduler~SettlementsData~ <|.. SettlementsProxy
    class SettlementsProxy {
        +get(MinecraftServer) SettlementsProxy$
    }
    
    class SettlementsData {
        ~long lastTick
        -int nextTownID
        -Map~Integer, Town~ towns
        +getTowns() Map~Integer, Town~
        +generateTownID() int
        +getTown(int id) @Nullable Town
        +getTown(Grid) Optional~Town~
        +getTowns(Grids) Iterable~Town~
        +addTown(Town)
        +Codec~SettlementsData~ CODEC$
    }

    SettlementsData "1" <-- "0..n" Town
    class Town {
        +getID() int
        +getName() String
        +setName(String)
        +getBorder() GridSquare
        +buildings() BuildingSet
        +getDimensionKey() ResourceKey~Level~
        +getReference() Function~SettlementsData, Town~
        +Codec~Town~ CODEC$
    }

    Town "1" <-- "1" BuildingSet
    class BuildingSet {
        ~Map~Grid, GridTerrain~ terrainMap
        ~Map~Grid, Buildable~ gridMap
        ~List~Buildable~ buildings
        +getTerrainMap() Map~Grid, GridTerrain~
        +getGridMap() Mao~Grid, Buildable~
        +getBuildings() Collection~Buildable~
        +scheduleRepairing(Buildable)
        +add(Buildable)
        +addAll(Collection~? extends Buildable~)
        +Codec~BuildingSet~ CODEC$
    }

    BuildingSet "1" <-- "0..n" Buildable
    class Buildable {
        <<abstract>>
        ...
    }
```

## 3. 建筑物与构造系统

全部建筑数据、行为及其实例抽象为 `Buildable` 及相关分层，实现类型注册、属性配置、世界定位与实例化。

- Buildable 和 BuildableStructure 为一切可建设物及其实例定义标准接口，实现类型、位置、创建流程与可序列化交互。
- Building、BuildingPattern、BuildingType、Building_BasicProperties 协同，将建筑类型、实例属性、结构和外观等切面进行细分，允许灵活扩展与多样性组合。
- BuildingLocation 精确表达建筑物在世界中的维度、坐标、方向等。
- BuildableType 及其子类，提供“注册型建筑种类”的管理、查找和工厂函数。
- 各抽象充分使用 Java 泛型，增强模块化与类型安全。
```mermaid
classDiagram
    class Buildable {
        <<interface>>
        getType() BuildableType~?, ?~
        getLocation() BuildingLocation~?~
        getName() String
        setName(String)
        build() BuildableStructure
        Codec~Buildable~ CODEC$
    }

    class BuildingLocation~G extends Grids~ {
        -ResourceKey~Level~ dimension
        -G grids
        -int groundY
        -Direction direction
        +dimension() ResourceKey~Level~
        +grids() G
        +groundY() int
        +direction() Direction
        +convert(Direction) Rotation$
        +convert(Rotation) Direction$
        +Codec~BuildingLocation~GridSquare~~ CODEC_GRID_SQUARE$
    }
    BuildingStructure ..> BuildingLocation
    Buildable ..> BuildingLocation

    BuildableStructure "1..n" <.. "1" Buildable
    class BuildableStructure {
        <<interface>>
        run(MinecraftServer) ...
    }

    Buildable <|.. Building~P extends Enum<P> & BuildingPattern~
    class Building~P extends Enum<P> & BuildingPattern~ {
        <<abstract>>
        +getType() BuildingType~...~
        -Building_BasicProperties properties
        -P buildingPattern
        +build() BuildingStructure
        +getBuildingPattern() P
        #setBuildingPattern(P)
    }

    Building "1" ..> "1" Building_BasicProperties
    class Building_BasicProperties {
        ~BuildingLocation~GridSquare~ location
        ~String name
        ~String buildingPattern
        +Codec~Building_BasicProperties~ CODEC$
    }

    BuildingPattern <-- Building
    class BuildingPattern {
        <<interface>>
        groundAt() int
        size() GridSize
        resourceKind() String
        resourceVariant() String
        resourceLocation() ResourceLocation
    }

    BuildableStructure <|.. BuildingStructure
    BuildingPattern <-- BuildingStructure
    BuildingStructure "1..n" <-- "1" Building
    class BuildingStructure {
        +BuildingPattern pattern
        +BuildingLocation~GridSquare~ location
    }

    BuildableType~B extends Buildable; E~ "n" <-- "1" Buildable
    class BuildableType~B extends Buildable; E~ {
        <<abstract>>
        +Class~B~ instanceClass
        +getName() String
        +getDisplayKey() String
        +getGridsPattern() GridsPattern~?~
        +select(BuildingSet) Selector~BuildingLocation~?~, E~
        #build(BuildingLocation~?~, E) B
        +codec() Codec~B~
        -Map~String, BuildableType~?, ?~ registeredTypes$
        +getType(String) BuildableType~?, ?~$
        +getTypes() Set~String~$
    }

    BuildingPattern <.. BuildingType~B extends Building<V>; V extends Enum<V> & BuildingPattern; E~
    BuildableType <|-- BuildingType
    BuildingType "n" <-- "1" Building
    class BuildingType~B extends Building<V>; V extends Enum<V> & BuildingPattern; E~ { 
        <<abstract>>
    }
```