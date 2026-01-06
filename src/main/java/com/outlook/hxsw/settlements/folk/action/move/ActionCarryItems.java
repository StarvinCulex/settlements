package com.outlook.hxsw.settlements.folk.action.move;

import com.outlook.hxsw.settlements.commercial.market.Stockpile;
import com.outlook.hxsw.settlements.engine.folks.pos.FolkPos;

public final class ActionCarryItems extends ActionAbstractMoveTo<Stockpile> {
    private final Stockpile items;

    // 要注意只有构造函数是在data thread调用的。所以items需要复制一份。
    public ActionCarryItems(FolkPos destination, Stockpile items) {
        super(destination);
        this.items = Stockpile.copyOf(items);
    }

    @Override
    protected Stockpile generateOutput() {
        return items;
    }
}
