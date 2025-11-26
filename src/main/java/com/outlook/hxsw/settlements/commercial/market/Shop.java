package com.outlook.hxsw.settlements.commercial.market;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.commercial.goods.*;
import java.util.*;

public class Shop extends Cashier {
    private final Set<Sellable> goodsList;

    public Shop(List<Sellable> goodsList) {
        this.goodsList = Set.copyOf(goodsList);
    }

    public Shop(List<Pair<Sellable, Integer>> items, List<Sellable> goodsList) {
        super(items);
        this.goodsList = Set.copyOf(goodsList);
    }

    public Set<Sellable> getGoods() {
        return goodsList;
    }

    public boolean sell(Sellable goods, int count, Stock wallet, Stock pack) {
        if (!getGoods().contains(goods)) {
            return false;
        }

        if (getCount(goods) < count) {
            return false;
        }
        double price = price(goods) * count;
        if (pay(wallet, price) == 0.) {
            return false;
        }
        pack.transfer(this, goods, count);
        return true;
    }

    public boolean sell(Sellable goods, int minCount, int maxCount, Stock wallet, Stock pack) {
        if (!getGoods().contains(goods)) {
            return false;
        }

        maxCount = Math.max(getCount(goods), maxCount);
        double walletPrice = evaluateWallet(wallet);
        double unitPrice = price(goods);
        int estimateCount = Math.min((int) (walletPrice / unitPrice), maxCount);
        if (estimateCount < minCount) {
            return false;
        }
        double estimatePrice = estimateCount * unitPrice;
        double payed = pay(wallet, estimatePrice);
        if (payed == 0.) {
            return false;
        }
        int count = Math.min((int) (payed / unitPrice), maxCount);
        pack.transfer(this, goods, count);
        return true;
    }

    public static final Codec<Shop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.pair(Sellable.CODEC, Codec.INT)).fieldOf("items").forGetter(Shop::getItemPairs),
            Codec.list(Sellable.CODEC).fieldOf("goodsList").forGetter(s ->  List.copyOf(s.goodsList))
    ).apply(instance, Shop::new));
}
