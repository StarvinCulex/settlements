package com.outlook.hxsw.settlements.commercial.pricing;

import com.outlook.hxsw.settlements.commercial.goods.*;
import com.outlook.hxsw.settlements.commercial.market.Stock;
import net.minecraft.world.item.Items;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

public interface PricingStrategy {
    Set<Sellable> currencies();
    double price(Sellable goods);

    PricingStrategy DEFAULT = new PricingStrategy() {
        static Map<Sellable, Double> CURRENCIES = Map.of(
                GoodsItem.of(Items.WHEAT), 1.,
                GoodsItem.of(Items.COPPER_INGOT), 50.,
                GoodsItem.of(Items.GOLD_INGOT), 5000.
        );

        static Map<Sellable, Double> PRICES = Map.of(

        );

        @Override
        public Set<Sellable> currencies() {
            return CURRENCIES.keySet();
        }

        @Override
        public double price(Sellable goods) {
            Double price = CURRENCIES.get(goods);
            if (price != null) {
                return price;
            }
            return PRICES.getOrDefault(goods, 0.);
        }
    };
}
