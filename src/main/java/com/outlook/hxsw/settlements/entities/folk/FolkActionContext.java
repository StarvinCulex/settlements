package com.outlook.hxsw.settlements.entities.folk;

import java.util.function.Consumer;

record FolkActionContext<A>(
    FolkAction<A> action,
    Consumer<A> finishCallback,
    Runnable unloadCallback,
    Consumer<FolkActionFailure> failCallback
) {
}
