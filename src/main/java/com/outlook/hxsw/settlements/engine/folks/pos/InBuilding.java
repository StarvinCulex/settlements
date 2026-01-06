package com.outlook.hxsw.settlements.engine.folks.pos;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSortedSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.buildings.Building;
import com.outlook.hxsw.settlements.engine.data.codecs.CodecType;
import com.outlook.hxsw.settlements.engine.data.id.BuildingID;
import com.outlook.hxsw.settlements.engine.grid.Grid;
import com.outlook.hxsw.settlements.engine.schedule.Condition;
import com.outlook.hxsw.settlements.engine.schedule.conditions.WhenChunkSimulated;
import com.outlook.hxsw.settlements.entities.folk.FolkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public final class InBuilding implements FolkPos {
    public final BuildingID<Building> buildingID;
    private final ResourceKey<Level> dimension;
    private final Grid grid;
    private final int groundY;
    private final int footingY;
    private final Set<Block> placeOn;

    private static final Duration EXPIRED_TIME = Duration.ofMinutes(5);
    private static final Cache<InBuilding, ImmutableSortedSet<BlockPos>> ACTION_POINTS_CACHE = CacheBuilder
            .newBuilder()
            .expireAfterWrite(EXPIRED_TIME)
            .build();

    public InBuilding(Building building, Set<Block> placeOn) {
        this.buildingID = BuildingID.of(building);
        this.dimension = building.getDimension();
        this.grid = building.getGrids().center();
        this.groundY = building.getGroundY();
        this.footingY = building.getFootingY();

        this.placeOn = placeOn;
    }

    @Override
    public ResourceKey<Level> dimension() {
        return dimension;
    }

    @Override
    public Grid grid() {
        return grid;
    }

    @Override
    public Optional<BlockPos> spawnPoint(Level level) {
        final EntityType<FolkEntity> folkType = FolkEntity.FOLK.get();
        double width  = folkType.getWidth();
        double height = folkType.getHeight();

        ImmutableSortedSet<BlockPos> actionPoints = actionPoints(level);

        int[] indexes = IntStream.range(0, actionPoints.size()).toArray();
        shuffle(indexes, level.random);

        for (int i : indexes) {
            BlockPos at = actionPoints.asList().get(i);
            BlockPos above = at.above();

            // Folk生成位置bounding box，位于(above.x+0.5, above.y, above.z+0.5)为中心
            AABB aabb = new AABB(
                    above.getX() + 0.5 - width/2, above.getY(), above.getZ() + 0.5 - width/2,
                    above.getX() + 0.5 + width/2, above.getY() + height, above.getZ() + 0.5 + width/2
            );
            boolean placeable = level.noCollision(aabb)
                    && FolkEntity.checkMobSpawnRules(folkType, level, EntitySpawnReason.EVENT, above, level.getRandom());
            if (placeable) {
                return Optional.of(at);
            }
        }

        return Optional.empty();
    }

    @Override
    public ImmutableSortedSet<BlockPos> actionPoints(Level level) {
        try {
            return ACTION_POINTS_CACHE.get(this, () -> this.calcActionPoint(level));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private ImmutableSortedSet<BlockPos> calcActionPoint(Level level) {
        List<BlockPos> points = new ArrayList<>();
        boolean allEmpty = false;
        for (int y = footingY; !allEmpty || y <= groundY; y++) {
            allEmpty = true;
            for (int x = grid.getFromX(); x <= grid.getToX(); x++) {
                for (int z = grid.getFromZ(); z <= grid.getToZ(); z++) {
                    BlockPos at = new BlockPos(x, y, z);
                    BlockState blockState = level.getBlockState(at);
                    if (!blockState.isEmpty()) {
                        allEmpty = false;
                    }
                    Block block = blockState.getBlock();
                    if (placeOn.contains(block)) {
                        points.add(at);
                    }
                }
            }
        }
        return ImmutableSortedSet.copyOf(points);
    }

    @Override
    public Condition actingCondition() {
        return new WhenChunkSimulated(dimension, grid.getChunk());
    }

    @Override
    public CodecType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public String toString() {
        return "InBuilding#" + buildingID;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof InBuilding b)) {
            return false;
        }

        return this.buildingID.equals(b.buildingID)
                && this.dimension.equals(b.dimension)
                && this.grid.equals(b.grid)
                && this.groundY == b.groundY
                && this.placeOn.equals(b.placeOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.buildingID,
                this.dimension,
                this.grid,
                this.groundY,
                this.placeOn
        );
    }

    private InBuilding(
            BuildingID<Building> buildingID,
            ResourceKey<Level> dimension,
            Grid grid,
            int groundY,
            int footingY,
            List<Block> placeOn
    ) {
        this.buildingID = buildingID;
        this.dimension = dimension;
        this.grid = grid;
        this.groundY = groundY;
        this.footingY = footingY;
        this.placeOn = Set.copyOf(placeOn);
    }

    public static final Codec<InBuilding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuildingID.CODEC.fieldOf("buildingID").forGetter(b -> b.buildingID),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(InBuilding::dimension),
            Grid.CODEC.fieldOf("grid").forGetter(InBuilding::grid),
            Codec.INT.fieldOf("groundY").forGetter(b -> b.groundY),
            Codec.INT.fieldOf("footingY").forGetter(b -> b.footingY),
            Codec.list(Block.CODEC.codec()).fieldOf("placeOn").forGetter(b -> List.copyOf(b.placeOn))
    ).apply(instance, InBuilding::new));

    static final class Type implements CodecType<InBuilding> {
        static final Type INSTANCE = new Type();

        @Override
        public String getName() {
            return "InBuilding";
        }

        @Override
        public Codec<InBuilding> codec() {
            return CODEC;
        }
    }

    private static void shuffle(int [] a, RandomSource random) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = a[i];
            a[i] = a[j];
            a[j] = swap;
        }
    }
}
