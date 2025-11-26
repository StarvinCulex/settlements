package com.outlook.hxsw.settlements.commercial.market;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.commercial.goods.Sellable;
import com.outlook.hxsw.settlements.commercial.pricing.PricingStrategy;

import java.util.*;
import java.util.function.ToIntFunction;

public class Cashier extends Stockpile {
    public Cashier() {}

    public Cashier(List<Pair<Sellable, Integer>> items) {
        super(items);
    }

    public final Set<Sellable> currencies() {
        return PricingStrategy.DEFAULT.currencies();
    }

    public final double price(Sellable item) {
        return PricingStrategy.DEFAULT.price(item);
    }

    public final double evaluateWallet(Stock wallet) {
        return evaluate(currencies().iterator(), wallet::getCount);
    }

    public Optional<Map<Sellable, Integer>> recommendPayment(Stock wallet, double price) {
        if (evaluateWallet(wallet) < price) {
            return Optional.empty();
        }
        return new PaymentSearch(wallet, price).search();
    }

    public double pay(Stock wallet, double price) {
        if (price <= 0) {
            return 0;
        }

        var payment = recommendPayment(wallet, price);
        if (payment.isEmpty()) {
            return 0.;
        }
        transfer(wallet, payment.get());
        return evaluate(payment.get().keySet().iterator(), payment.get()::get);
    }

    protected double evaluate(Iterator<Sellable> items, ToIntFunction<Sellable> counts) {
        double sum = 0;
        while (items.hasNext()) {
            Sellable item = items.next();
            sum += counts.applyAsInt(item) * this.price(item);
        }
        return sum;
    }

    private final class PaymentSearch {
        private final Sellable[] coins;
        private final Stock wallet;
        private final double expectedPrice;
        private final double minimumCurrency;

        PaymentSearch(Stock wallet, double expectedPrice) {
            this.wallet = wallet;
            this.coins = currencies().toArray(Sellable[]::new);
            Arrays.sort(coins, Comparator.comparingDouble(Cashier.this::price).reversed());
            this.expectedPrice = expectedPrice;
            this.minimumCurrency = coins.length != 0 ? price(coins[coins.length - 1]) : 0;
            this.plan = new int[coins.length];
            this.bestPlan = new int[coins.length];
        }

        Optional<Map<Sellable, Integer>> search() {
            dfs(0, 0);
            Map<Sellable, Integer> order = new HashMap<>();
            for (int i = 0; i < coins.length; ++i) {
                if (bestPlan[i] != 0) {
                    order.put(coins[i], bestPlan[i]);
                }
            }
            return order.isEmpty() ? Optional.empty() : Optional.of(order);
        }

        private final int[] plan;
        // 返回值表示已经找到最优解，可以终止搜索
        private boolean dfs(int index, double currentPrice) {
            if (index == coins.length) {
                return submitPlan(currentPrice);
            }

            Sellable coin = coins[index];
            double unitPrice = price(coin);
            ZigzagIterator countIterator = new ZigzagIterator(
                    (int) ((expectedPrice - currentPrice) / unitPrice),
                    -getCount(coin), wallet.getCount(coin)
            );
            while (countIterator.hasNext()) {
                int count = countIterator.next();
                plan[index] = count;
                if (dfs(index + 1, currentPrice + count * unitPrice)) return true;
            }
            return false;
        }

        private final int[] bestPlan;
        private double bestPrice = 0;
        private boolean submitPlan(double currentPrice) {
            if (currentPrice < this.expectedPrice) return false;
            if (bestPrice - expectedPrice <= currentPrice - expectedPrice) return false;
            bestPrice = currentPrice;
            System.arraycopy(plan, 0, bestPlan, 0, bestPlan.length);
            return currentPrice - expectedPrice <= minimumCurrency;
        }

        private static final class ZigzagIterator {
            private final int begin;
            private final int min;
            private final int max;
            private final int size;
            private int step = 0;
            private int offset = 0;

            ZigzagIterator(int begin, int min, int max) {
                this.begin = Math.clamp(begin, min, max);
                this.min = min;
                this.max = max;
                this.size = max - min + 1;
            }

            public boolean hasNext() {
                return step < size;
            }

            public int next() {
                ++step;
                int r;
                do {
                    r = offset + begin;
                    offset = -offset - (offset >= 0 ? 1 : 0);
                } while (r < min || max < r);
                return r;
            }
        }
    }

    public static final Codec<Cashier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.pair(Sellable.CODEC, Codec.INT)).fieldOf("items").forGetter(Cashier::getItemPairs)
    ).apply(instance, Cashier::new));
}
