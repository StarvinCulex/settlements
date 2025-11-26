package com.outlook.hxsw.settlements.engine.folks.job;

public final class Jobs<I, O> {
    private JobNode<I, ?> head;
    private JobNode<?, O> tail;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <H> Jobs<H, O> before(Job<H, I> job) {
        Jobs that = this;
        that.head = new JobNode<>(job, that.head);
        return that;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R> Jobs<I, R> then(Job<O, R> job) {
        Jobs that = this;
        that.tail.next = new JobNode<>(job, null);
        that.tail = that.tail.next;
        return that;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R> Jobs<I, R> then(Jobs<O, R> jobs) {
        Jobs that = this;
        that.tail.next = jobs.head;
        that.tail = jobs.tail;
        return that;
    }

    Jobs(Job<I, O> job) {
        JobNode<I, O> node = new JobNode<>(job, null);
        this.head = node;
        this.tail = node;
    }

    <J> Jobs(Job<I, J> j1, Job<J, O> j2) {
        var tail = new JobNode<>(j2, null);
        this.tail = tail;
        this.head = new JobNode<>(j1, tail);
    }

    JobNode<I, ?> intoNode() {
        return head;
    }
}
