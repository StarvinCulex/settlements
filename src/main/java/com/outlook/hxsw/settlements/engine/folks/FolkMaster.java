package com.outlook.hxsw.settlements.engine.folks;

import com.outlook.hxsw.settlements.engine.data.id.FolkID;
import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;
import com.outlook.hxsw.settlements.engine.data.utils.WithParent;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import java.util.*;
import java.util.stream.Stream;

public abstract class FolkMaster<P extends SidecarData> extends WithParent<P> {
    private final Set<FolkID> folks = new HashSet<>();

    public FolkMaster(Class<P> parentClass) {
        super(parentClass);
    }

    public FolkMaster(Class<P> parentClass, Stream<FolkID> folks) {
        super(parentClass);
        folks.forEach(this.folks::add);
    }

    public final Collection<FolkID> getFolks() {
        return Collections.unmodifiableCollection(folks);
    }

    protected void add(Folk folk) {
        if (!folks.add(FolkID.of(folk))) {
            throw new IllegalArgumentException();
        }
    }

    protected boolean tryRemove(Folk folk) {
        return folks.remove(FolkID.of(folk));
    }

    protected Optional<Folk> tryRemove() {
        var it = folks.iterator();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        var id = it.next();
        folks.remove(id);
        return id.get(scheduler().data().folks);
    }

    @Override
    public void registerToDataScheduler(DataScheduler scheduler) {
        for (var id : folks) {
            Folk folk = id.get(scheduler.data().folks).orElseThrow();
            folk.master = this;
        }
    }
}
