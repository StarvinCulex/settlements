package com.outlook.hxsw.settlements.building.utils.stock;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.outlook.hxsw.settlements.engine.goods.Sellable;

import java.util.*;
import java.util.stream.Collectors;

public class Stockpile {
    protected final Map<Sellable, Integer> items;

    public Stockpile() {
        this.items = new HashMap<>();
    }

    public Stockpile(Map<Sellable, Integer> items) {
        this.items = items;
    }

    public Map<Sellable, Integer> getItemCount() {
        return Collections.unmodifiableMap(items);
    }

    public void storeItem(Sellable item, int count) {
        takeAllItem(item, -count);
    }

    public int takeAllItem(Sellable item) {
        return Objects.requireNonNullElse(items.put(item, 0), 0);
    }

    public int takeAllItem(Sellable item, int maxCount) {
        int n = items.getOrDefault(item, 0);
        if (n <= maxCount) {
            items.put(item, 0);
            maxCount = n;
        } else {
            items.put(item, n - maxCount);
        }
        return maxCount;
    }

    public boolean takeItem(Sellable item, int acquiringCount) {
        return null != items.computeIfPresent(item,
                (k, c) -> c < acquiringCount ? null : c - acquiringCount
        );
    }

    public static final Codec<Stockpile> CODEC = Codec.list(Codec.pair(Sellable.CODEC, Codec.INT)).xmap(
            l -> new Stockpile(l.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))),
            stockpile -> stockpile.items.entrySet().stream()
                    .map(e -> Pair.of(e.getKey(), e.getValue())).toList()
    );
}
