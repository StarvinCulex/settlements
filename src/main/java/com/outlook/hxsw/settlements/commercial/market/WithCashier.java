package com.outlook.hxsw.settlements.commercial.market;

public interface WithCashier extends WithStockpile {
    @Override
    Cashier getStock();
}
