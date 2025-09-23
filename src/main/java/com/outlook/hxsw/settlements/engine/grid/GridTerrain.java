package com.outlook.hxsw.settlements.engine.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Arrays;
import java.util.function.BiFunction;

public final class GridTerrain {
    private int groundHeight;
    private float flat;

    private GridTerrain() {}

    public GridTerrain(
            int groundHeight,
            float flat
    ) {
        this.groundHeight = groundHeight;
        this.flat = flat;
    }

    public int groundHeight() {
        return groundHeight;
    }

    public float flat() {
        return flat;
    }

    public boolean placable() {
        return flat < 10;
    }

    public record Surveyor(Grid grid) {
        public GridTerrain survey(ServerLevel level) {
            var gen = level.getChunkSource().getGenerator();
            var randomState = level.getChunkSource().randomState();
            GridTerrain dest = new GridTerrain();

            surveyGroundHeight(dest, (x, z) ->
                    gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE, level, randomState) - 1);

            return dest;
        }

        // 这个方法太慢了。目前一格城镇的地形勘察就需要30s，会卡死随机刻。需要优化。
        private void surveyGroundHeight(GridTerrain dest, BiFunction<Integer, Integer, Integer> heightGetter) {
            int[] height = new int[32];
            int i = 0;
            for (int x = grid.getFromX(); x <= grid.getToX(); x++) {
                height[i++] = heightGetter.apply(x, grid.getFromZ());
                height[i++] = heightGetter.apply(x, grid.getToZ());
            }
            for (int z = grid.getFromZ(); z <= grid.getToZ(); z++) {
                height[i++] = heightGetter.apply(grid.getFromX(), z);
                height[i++] = heightGetter.apply(grid.getToX(), z);
            }
            Arrays.sort(height);
            dest.groundHeight = height[15];
            dest.flat = calcVariance(height, dest.groundHeight);
        }

        private static float calcVariance(int[] data, int standard) {
            float sum = 0;
            for (int d : data) {
                float delta = d - standard;
                sum += delta * delta;
            }
            return sum / data.length;
        }
    }

    @Override
    public String toString() {
        return "groundHeight=" + groundHeight + ", flat=" + flat;
    }

    public static final Codec<GridTerrain> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("groundHeight").forGetter(GridTerrain::groundHeight),
            Codec.FLOAT.fieldOf("flat").forGetter(GridTerrain::flat)
    ).apply(instance, GridTerrain::new));
}
