package com.outlook.hxsw.settlements.engine.folks.job;

import java.util.Objects;

/**
 * 每个Jobs对象只能派发一个Work。
 */
public final class Jobs<I, O> {
    private JobNode<I, ?, ?> head;
    private JobNode<?, ?, O> tail;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <H> Jobs<H, O> before(Job<H, ?, I> job) {
        check();
        Jobs that = this;
        that.head = new JobNode<>(job, that.head);
        return that;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R> Jobs<I, R> then(Job<O, ?, R> job) {
        check();
        Jobs that = this;
        that.tail.next = new JobNode<>(job, null);
        that.tail = that.tail.next;
        return that;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R> Jobs<I, R> then(Jobs<O, R> jobs) {
        check();
        Jobs that = this;
        that.tail.next = jobs.head;
        that.tail = jobs.tail;
        jobs.head = null;
        return that;
    }

    Jobs(Job<I, ?, O> job) {
        JobNode<I, ?, O> node = new JobNode<>(job, null);
        this.head = node;
        this.tail = node;
    }

    <J> Jobs(Job<I, ?, J> j1, Job<J, ?, O> j2) {
        var tail = new JobNode<>(j2, null);
        this.tail = tail;
        this.head = new JobNode<>(j1, tail);
    }

    JobNode<I, ?, ?> export() {
        JobNode<I, ?, ?> node = head;
        head = null;
        return node;
    }

    private void check() {
        Objects.requireNonNull(head, "Jobs has been used. Cannot modify.");
    }
}
