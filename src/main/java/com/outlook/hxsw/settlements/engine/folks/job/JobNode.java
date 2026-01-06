package com.outlook.hxsw.settlements.engine.folks.job;

import com.mojang.serialization.Codec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

final class JobNode<I, A, O> {
    final Job<I, A, O> job;
    @Nullable JobNode<O, ?, ?> next;

    JobNode(Job<I, A, O> job, @Nullable JobNode<O, ?, ?> next) {
        this.job = job;
        this.next = next;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static @Nullable JobNode<?, ?, ?> fromList(List<Job<?, ?, ?>> jobs) {
        @Nullable JobNode node = null;
        for (var job : jobs.reversed()) {
            node = new JobNode(job, node);
        }
        return node;
    }

    static List<Job<?, ?, ?>> intoList(@Nullable JobNode<?, ?, ?> node) {
        var list = new ArrayList<Job<?, ?, ?>>();
        while (node != null) {
            list.add(node.job);
            node = node.next;
        }
        return list;
    }

    static <I> Codec<I> argCodec(@Nullable JobNode<I, ?, ?> node) {
        return node == null ? Codec.unit(null) : node.job.inputCodec();
    }
}
