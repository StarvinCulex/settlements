package com.outlook.hxsw.settlements.engine.folks.job;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.outlook.hxsw.settlements.engine.data.codecs.DecodeException;
import com.outlook.hxsw.settlements.engine.schedule.DataScheduler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

record JobNodeWithInput<I, A, O>(I input, @Nullable JobNode<I, A, O> node) {
    public static Codec<JobNodeWithInput<?, ?, ?>> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<JobNodeWithInput<?, ?, ?>, T>> decode(DynamicOps<T> ops, T input) {
            try {
                T jobsInput = ops.get(input, "jobs").getOrThrow(DecodeException::new);
                List<Job<?, ?, ?>> jobs = Codec.list(Job.CODEC).decode(ops, jobsInput)
                        .getOrThrow(DecodeException::new).getFirst();
                JobNode<?, ?, ?> node = JobNode.fromList(jobs);

                T argInput = ops.get(input, "arg").getOrThrow(DecodeException::new);
                Pair<?, T> argPair = JobNode.argCodec(node).decode(ops, argInput).getOrThrow(DecodeException::new);

                @SuppressWarnings({"rawtypes", "unchecked"})
                JobNodeWithInput<?, ?, ?> r = new JobNodeWithInput(argPair.getFirst(), node);
                return DataResult.success(new Pair<>(r, argPair.getSecond()));
            } catch (DecodeException ex) {
                return DataResult.error(ex);
            }
        }

        @Override
        public <T> DataResult<T> encode(JobNodeWithInput<?, ?, ?> input, DynamicOps<T> ops, T prefix) {
            return encodeHelper(input, ops, prefix);
        }

        private <J, T> DataResult<T> encodeHelper(JobNodeWithInput<J, ?, ?> input, DynamicOps<T> ops, T prefix) {
            var builder = ops.mapBuilder();
            builder.add("jobs", JobNode.intoList(input.node), Codec.list(Job.CODEC));
            builder.add("arg", input.input, JobNode.argCodec(input.node));
            return builder.build(prefix);
        }
    };
}
