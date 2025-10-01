package com.outlook.hxsw.settlements.engine.schedule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@FunctionalInterface
public interface Condition extends Predicate<ServerScheduler> {
    default Collection<Trigger> triggers() {
        return List.of();
    }

    default Condition negate() {
        return s -> !this.test(s);
    }

    default Condition and(Condition other) {
        return Condition.and(this, other);
    }

    default Condition or(Condition other) {
        return Condition.or(this, other);
    }

    static Condition and(Condition... conditions) {
        Collection<Trigger> triggers = Arrays.stream(conditions)
                .map(Condition::triggers)
                .filter(Predicate.not(Collection::isEmpty))
                .min(Comparator.comparingInt(Collection::size))
                .orElseGet(List::of);

        return new Condition() {
            @Override
            public boolean test(ServerScheduler scheduler) {
                return Arrays.stream(conditions).allMatch(c -> c.test(scheduler));
            }

            @Override
            public Collection<Trigger> triggers() {
                return triggers;
            }
        };
    }

    static Condition or(Condition... conditions) {
        Collection<Trigger> triggers = Arrays.stream(conditions)
                .flatMap(c -> c.triggers().stream())
                .toList();

        return new Condition() {
            @Override
            public boolean test(ServerScheduler scheduler) {
                return Arrays.stream(conditions).anyMatch(c -> c.test(scheduler));
            }

            @Override
            public Collection<Trigger> triggers() {
                return triggers;
            }
        };
    }
}
