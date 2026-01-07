package com.outlook.hxsw.settlements.engine.data;

import com.outlook.hxsw.settlements.engine.folks.FolkShareZone;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SettlementsTable {
    private final ConcurrentHashMap<Integer, FolkShareZone> folkZone;

    public SettlementsTable(ConcurrentHashMap<Integer, FolkShareZone> folkZone) {
        this.folkZone = folkZone;
    }

    public Optional<FolkShareZone> getFolkShareZone(int id) {
        return Optional.ofNullable(folkZone.get(id));
    }
}
