package com.outlook.hxsw.settlements.commercial.market;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.commercial.goods.Sellable;

import java.util.*;

public class Stockpile implements Stock {
    private int count = 0;
    private final Map<Sellable, Integer> items = new HashMap<>();

    public Stockpile() {}

    public Stockpile(List<Pair<Sellable, Integer>> items) {
        for (var pair : items) {
            this.items.put(pair.getFirst(), pair.getSecond());
            this.count += pair.getSecond();
        }
    }

    public Stockpile(Map<Sellable, Integer> items) {
        this.count = items.values().stream().reduce(0, Integer::sum);
        this.items.putAll(items);
    }

    public static Stockpile copyOf(Stockpile src) {
        Stockpile dest = new Stockpile();
        dest.count = src.count;
        dest.items.putAll(src.items);
        return dest;
    }

    public final boolean isEmpty() {
        return count == 0;
    }

    public final int getCount() {
        return count;
    }

    @Override
    public int getCount(Sellable item) {
        return this.items.getOrDefault(item, 0);
    }

    @Override
    public void store(Sellable item, int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        this.items.compute(item, (i, n) -> n == null ? count : n + count);
    }

    @Override
    // 返回实际拿走的物品数量
    public int take(Sellable item, int maxCount) {
        if (maxCount < 0) {
            throw new IllegalArgumentException();
        }
        int taken;
        int n = this.items.getOrDefault(item, 0);
        if (n <= maxCount) {
            this.items.remove(item);
            taken = n;
        } else {
            this.items.put(item, n - maxCount);
            taken = maxCount;
        }
        this.count -= taken;
        return taken;
    }

    public void takeAll(Stock dest) {
        dest.store(items);
        this.items.clear();
        this.count = 0;
    }

    protected final List<Pair<Sellable, Integer>> getItemPairs() {
        return this.items.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList();
    }

    public static final Codec<Stockpile> CODEC = Codec.list(Codec.pair(Sellable.CODEC, Codec.INT))
            .xmap(Stockpile::new, Stockpile::getItemPairs);
}
