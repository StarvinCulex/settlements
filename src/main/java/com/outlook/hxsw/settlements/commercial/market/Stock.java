package com.outlook.hxsw.settlements.commercial.market;

import com.outlook.hxsw.settlements.commercial.goods.Sellable;

import java.util.*;

public interface Stock {
    int getCount(Sellable item);

    void store(Sellable item, int count);

    // 返回实际拿走的物品数量
    int take(Sellable item, int maxCount);

    default void store(Map<Sellable, Integer> items) {
        for (var entry : items.entrySet()) {
            store(entry.getKey(), entry.getValue());
        }
    }

    default int take(Map<Sellable, Integer> items) {
        int taken = 0;
        for (var entry : items.entrySet()) {
            taken += take(entry.getKey(), entry.getValue());
        }
        return taken;
    }

    default void transfer(Stock src, Sellable item, int delta) {
        if (delta < 0) {
            src.store(item, this.take(item, -delta));
        } else {
            this.store(item, src.take(item, delta));
        }
    }

    default void transfer(Stock src, Map<Sellable, Integer> items) {
        for (var entry : items.entrySet()) {
            transfer(src, entry.getKey(), entry.getValue());
        }
    }

    default Stock filter(Map<Sellable, Integer> itemsLimit) {
        return new Stock() {
            @Override
            public int getCount(Sellable item) {
                return Math.max(0, Math.min(
                        Stock.this.getCount(item),
                        itemsLimit.getOrDefault(item, 0)
                ));
            }

            @Override
            public void store(Sellable item, int count) {
                Stock.this.store(item, count);
            }

            @Override
            public int take(Sellable item, int maxCount) {
                int count = Math.max(0, Math.min(maxCount, itemsLimit.getOrDefault(item, 0)));
                return Stock.this.take(item, count);
            }
        };
    }
}
