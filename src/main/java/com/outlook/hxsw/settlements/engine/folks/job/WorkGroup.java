package com.outlook.hxsw.settlements.engine.folks.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.outlook.hxsw.settlements.engine.data.utils.SidecarData;
import com.outlook.hxsw.settlements.engine.data.id.FolkID;
import com.outlook.hxsw.settlements.engine.folks.*;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WorkGroup<P extends SidecarData> extends FolkMaster<P> implements WorkCallback {
    private final Map<FolkID, Work> activatingWorks = new HashMap<>();  // 这里的work有workerID
    private final Queue<Work> waitingWorks = new ArrayDeque<>(); // 这里的work都没有workerID
    private final Queue<FolkID> idleWorkers = new ArrayDeque<>();

    public WorkGroup(Class<P> parentClass) {
        super(parentClass);
    }

    public WorkGroup(Class<P> parentClass, List<Work> works, List<FolkID> idleWorkers) {
        super(parentClass, Stream.concat(
                works.stream().map(Work::getWorkerID).filter(Predicate.not(FolkID::isEmpty)),
                idleWorkers.stream()
        ));

        for (Work work : works) {
            if (work.workerID.isEmpty()) {
                this.waitingWorks.add(work);
            } else {
                this.activatingWorks.put(work.workerID, work);
            }
        }
        this.idleWorkers.addAll(idleWorkers);
    }

    public <I> Work addWork(I input, Jobs<I, ?> jobs) {
        return addWork(new JobNodeWithInput<>(input, jobs.export()));
    }

    public <I> Work addWork(I input, Job<I, ?, ?> job) {
        return addWork(new JobNodeWithInput<>(input, new JobNode<>(job, null)));
    }

    public Work addWork(Jobs<Void, ?> jobs) {
        return addWork(null, jobs);
    }

    public Work addWork(Job<Void, ?, ?> job) {
        return addWork(null, job);
    }

    public List<Work> getWorks() {
        return Stream.concat(
                activatingWorks.values().stream(),
                waitingWorks.stream()
        ).toList();
    }

    public List<Folk> getBusyWorkers() {
        return activatingWorks.keySet().stream().map(id -> id.get(scheduler().data().folks).orElseThrow()).toList();
    }

    public List<Folk> getIdleWorkers() {
        return idleWorkers.stream().map(id -> id.get(scheduler().data().folks).orElseThrow()).toList();
    }

    @Override
    protected void add(Folk folk) {
        super.add(folk);
        idleWorkers.add(FolkID.of(folk));
        consume(Objects.requireNonNull(scheduler()));
    }

    /**
     * 忙碌状态的folk无法remove
     */
    @Override
    protected boolean tryRemove(Folk folk) {
        var id = FolkID.of(folk);
        Work work = activatingWorks.get(id);
        if (work != null) {
            work.cancel(scheduler());
        }
        return idleWorkers.remove(id) && super.tryRemove(folk);
    }

    @Override
    protected Optional<Folk> tryRemove() {
        FolkID id;
        if (idleWorkers.isEmpty()) {
            var it = activatingWorks.keySet().iterator();
            if (!it.hasNext()) {
                return Optional.empty();
            }
            id = it.next();
            activatingWorks.remove(id);
        } else {
            id = idleWorkers.remove();
        }
        var folk = id.get(scheduler().data().folks);
        folk.ifPresent(super::tryRemove);
        return folk;
    }

    private Work addWork(JobNodeWithInput<?, ?, ?> job) {
        Work work = new Work(job);
        work.addCallback(this);
        waitingWorks.add(work);
        consume(Objects.requireNonNull(scheduler()));
        return work;
    }

    private void consume(DataScheduler scheduler) {
        while (!waitingWorks.isEmpty() && !idleWorkers.isEmpty()) {
            var folkID = idleWorkers.remove();
            var work = waitingWorks.remove();
            work.setWorkerID(folkID);
            activatingWorks.put(folkID, work);
            work.run(scheduler);
        }
    }

    @Override
    protected void onRegistering(DataScheduler scheduler) {
        super.onRegistering(scheduler);
        for (Work work : activatingWorks.values()) {
            work.run(scheduler);
        }
    }

    @Override
    public final void onFinish(DataScheduler scheduler, Work work) {
        var workerID = work.workerID;
        activatingWorks.remove(workerID);
        idleWorkers.add(workerID);
        consume(scheduler);
    }

    protected List<FolkID> getIdleWorkerIDs() {
        return List.copyOf(idleWorkers);
    }

    public static <P extends SidecarData> Codec<WorkGroup<P>> codec(Class<P> parentClass) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(Work.CODEC).fieldOf("works").forGetter(WorkGroup::getWorks),
                Codec.list(FolkID.CODEC).fieldOf("idleWorkers").forGetter(WorkGroup::getIdleWorkerIDs)
        ).apply(instance, (a, b) -> new WorkGroup<>(parentClass, a, b)));
    }
}
