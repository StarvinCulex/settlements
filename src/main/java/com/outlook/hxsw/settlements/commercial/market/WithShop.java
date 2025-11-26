package com.outlook.hxsw.settlements.commercial.market;

public interface WithShop extends WithCashier {
    @Override
    Shop getStock();
}
