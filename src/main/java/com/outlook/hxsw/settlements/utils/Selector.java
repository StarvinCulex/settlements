package com.outlook.hxsw.settlements.utils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public interface Selector<E, O> extends Predicate<E>, Function<E, Optional<O>> {
    Stream<Result<E, O>> stream();

    record Result<E, O>(E element, O extra) implements Supplier<E> {
        @Override
        public E get() {
            return element;
        }
    }
    record Pair<F, S>(F first, S second) { }
    record Triple<F, S, T>(F first, S second, T third) { }

    default Selector<E, O> filter(Predicate<E> filter) {
        return new Filter<>(this, (e, o) -> filter.test(e));
    }

    default Selector<E, O> filter(BiPredicate<E, O> filter) {
        return new Filter<>(this, filter);
    }

    @SafeVarargs
    static <E, O> Selector<E, O> chained(Selector<E, O>... selectors) {
        return new Chain<>(selectors);
    }

    default Selector<E, O> chain(Selector<E, O> other) {
        return chained(this, other);
    }

    default <P> Selector<E, P> then(Function<E, Optional<P>> other) {
        return new Then<>(this, other);
    }

    default <P> Selector<E, P> extraMap(Function<O, P> mapper) {
        return new ExtraMap<>(this, mapper);
    }

    default <P> Selector<E, Pair<O, P>> join(Function<E, Optional<P>> second) {
        return new JoinPair<>(this, (e, o) -> second.apply(e));
    }

    default <P> Selector<E, Pair<O, P>> join(BiFunction<E, O, Optional<P>> second) {
        return new JoinPair<>(this, second);
    }

    @SafeVarargs
    static <E> Selector<E, Boolean> of(E... elements) {
        return new Of<>(elements);
    }

    @Override
    default boolean test(E element) {
        return apply(element).isPresent();
    }
}


record Filter<E, O>(Selector<E, O> selector, BiPredicate<E, O> filter) implements Selector<E, O> {
    @Override
    public Optional<O> apply(E element) {
        var v = selector.apply(element);
        if (v.isPresent() && !filter.test(element, v.get())) {
            return Optional.empty();
        }
        return v;
    }

    @Override
    public Stream<Result<E, O>> stream() {
        return selector.stream().filter(o -> filter.test(o.element(), o.extra()));
    }
}


record Chain<E, O>(Selector<E, O>[] selectors) implements Selector<E, O> {
    @Override
    public Optional<O> apply(E element) {
        for (var s : selectors) {
            var extra = s.apply(element);
            if (extra.isPresent()) {
                return extra;
            }
        }
        return Optional.empty();
    }

    @Override
    public Stream<Result<E, O>> stream() {
        return Arrays.stream(selectors).map(Selector::stream).reduce(Stream::concat).orElse(Stream.empty());
    }
}


record Then<E, O>(Selector<E, ?> condition, Function<E, Optional<O>> selector) implements Selector<E, O> {
    @Override
    public Optional<O> apply(E element) {
        if (condition.apply(element).isEmpty()) {
            return Optional.empty();
        }
        return selector.apply(element);
    }

    @Override
    public Stream<Result<E, O>> stream() {
        return condition.stream()
                .map(Result::element)
                .flatMap(e -> selector.apply(e).map(o -> new Result<>(e, o)).stream());
    }
}


record ExtraMap<E, O, P>(Selector<E, O> selector, Function<O, P> mapper) implements Selector<E, P> {
    @Override
    public Optional<P> apply(E element) {
        return selector.apply(element).map(mapper);
    }

    @Override
    public Stream<Result<E, P>> stream() {
        return selector.stream().map(r -> new Result<>(r.element(), mapper.apply(r.extra())));
    }
}


record JoinPair<E, O, P>(Selector<E, O> first, BiFunction<E, O, Optional<P>> second) implements Selector<E, Selector.Pair<O, P>> {
    @Override
    public Optional<Pair<O, P>> apply(E element) {
        return first.apply(element).flatMap(f -> second.apply(element, f).map(s -> new Pair<>(f, s)));
    }

    @Override
    public Stream<Result<E, Pair<O, P>>> stream() {
        return first.stream().flatMap(r -> second.apply(
                        r.element(), r.extra()).map(
                        s -> new Result<>(
                                r.element(), new Pair<>(r.extra(), s)
                        )
                ).stream()
        );
    }
}


record Of<E>(E[] elements) implements Selector<E, Boolean> {
    @Override
    public Stream<Result<E, Boolean>> stream() {
        return Stream.of(elements).map(e -> new Result<>(e, true));
    }

    @Override
    public Optional<Boolean> apply(E e) {
        if (Arrays.asList(elements).contains(e)) {
            return Optional.of(true);
        }
        return Optional.empty();
    }
}
