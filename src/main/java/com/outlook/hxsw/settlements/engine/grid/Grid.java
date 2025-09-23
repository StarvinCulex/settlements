package com.outlook.hxsw.settlements.engine.grid;


import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * 表示基于(u, v)坐标网络的一个网格。是此模组建筑占用土地的最小单元。
 * <p>
 * 这个网格表示了水平面上 8×8 的格子。一个游戏区块刚好包含四个Grid。
 * </p>
 */
public final class Grid {
    public final int u;
    public final int v;

    /**
     * 根据区块坐标和它的一个角构造Grid。
     * @param chunkX 区块的X坐标
     * @param chunkZ 区块的Z坐标
     * @param chunkCorner 区块的角
     */
    public Grid(int chunkX, int chunkZ, ChunkCorner chunkCorner) {
        this((chunkX << 1) + chunkCorner.u, (chunkZ << 1) + chunkCorner.v);
    }

    public Grid(Vec3 pos) {
        this(BlockPos.containing(pos));
    }

    public Grid(Vec3i pos) {
        this(pos.getX() >> 3, pos.getZ() >> 3);
    }

    Grid(int u, int v) {
        this.u = u;
        this.v = v;
    }

    /**
     * @return 表示区域格点的最小的X坐标
     */
    public int getFromX() {
        return u << 3;
    }
    /**
     * @return 表示区域格点的最小的Z坐标
     */
    public int getFromZ() {
        return v << 3;
    }
    
    /**
     * @return 表示区域格点的最大的X坐标
     */
    public int getToX() {
        return getFromX() + 7;
    }
    
    /**
     * @return 表示区域格点的最大的Z坐标
     */
    public int getToZ() {
        return getFromZ() + 7;
    }
    
    /**
     * @return 所在区块的X坐标
     */
    public int getChunkX() {
        return u >> 1;
    }

    /**
     * @return 所在区块的X坐标
     */
    public int getChunkZ() {
        return v >> 1;
    }

    public ChunkPos getChunk() {
        return new ChunkPos(getChunkX(), getChunkZ());
    }

    /**
     * @return 所在区块的角的方位
     */
    public ChunkCorner getChunkCorner() {
        return ChunkCorner.values()[(u & 1) + 2 * (v & 1)];
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Grid g && u == g.u && v == g.v;
    }

    @Override
    public int hashCode() {
        return Objects.hash(u, v);
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%s", getChunkX(), getChunkZ(), getChunkCorner());
    }

    /**
     * 将toString方法的结果反解析成Grid。
     * @param formatted 从toString()来的字符串
     * @return 反解析成的Grid对象。
     */
    public static Grid parse(String formatted) {
        int firstComma = formatted.indexOf(',');
        int secondComma = formatted.indexOf(',', firstComma + 1);
        if (firstComma < 0 || secondComma < 0)
            throw new IllegalArgumentException("Invalid grid string: " + formatted);

        int chunkX = Integer.parseInt(formatted.substring(0, firstComma));
        int chunkZ = Integer.parseInt(formatted.substring(firstComma + 1, secondComma));
        ChunkCorner corner = ChunkCorner.valueOf(formatted.substring(secondComma + 1));
        return new Grid(chunkX, chunkZ, corner);
    }


    /** Codec.unboundedMap 只能接受可字符串化的key，这个实现可直接用unboundedMap序列化<code>Map&lt;Grid,?&gt;</code>。 */
    public static final Codec<Grid> CODEC = Codec.STRING.xmap(Grid::parse, Grid::toString);

    public enum ChunkCorner {
        NW(0, 0),
        NE(1, 0),
        SW(0, 1),
        SE(1, 1);

        private final int u;
        private final int v;
        ChunkCorner(int u, int v) {
            this.u = u;
            this.v = v;
        }

        public static Codec<ChunkCorner> CODEC = Codec.STRING.xmap(ChunkCorner::valueOf, ChunkCorner::name);
    }
}
