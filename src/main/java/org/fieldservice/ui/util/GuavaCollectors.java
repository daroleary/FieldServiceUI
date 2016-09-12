package org.fieldservice.ui.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class GuavaCollectors {
    private GuavaCollectors() {
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (value, another) -> {
            throw new IllegalStateException(String.format("Duplicated value %s", value));
        };
    }

    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        return new Collector<T, Builder<T>, ImmutableList<T>>() {

            @Override
            public Supplier<Builder<T>> supplier() {
                return ImmutableList::builder;
            }

            @Override
            public BiConsumer<Builder<T>, T> accumulator() {
                return Builder::add;
            }

            @Override
            public BinaryOperator<Builder<T>> combiner() {
                return (builder, another) -> builder.addAll(another.build());
            }

            @Override
            public Function<Builder<T>, ImmutableList<T>> finisher() {
                return Builder::build;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return ImmutableSet.of();
            }
        };
    }

    public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
        return new Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>>() {
            @Override
            public Supplier<ImmutableSet.Builder<T>> supplier() {
                return ImmutableSet::builder;
            }

            @Override
            public BiConsumer<ImmutableSet.Builder<T>, T> accumulator() {
                return ImmutableSet.Builder::add;
            }

            @Override
            public BinaryOperator<ImmutableSet.Builder<T>> combiner() {
                return (builder, another) -> builder.addAll(another.build());
            }

            @Override
            public Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher() {
                return ImmutableSet.Builder::build;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return ImmutableSet.of();
            }
        };
    }

    public static <T, K, U> Collector<T, ?, ImmutableMap<K, U>> toImmutableMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return toImmutableMap(keyMapper, valueMapper, throwingMerger());
    }

    public static <T, K, U> Collector<T, ?, ImmutableMap<K, U>> toImmutableMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper,
            BinaryOperator<U> mergeFunction) {

        return new Collector<T, Map<K, U>, ImmutableMap<K, U>>() {
            @Override
            public Supplier<Map<K, U>> supplier() {
                return HashMap::new;
            }

            @Override
            public BiConsumer<Map<K, U>, T> accumulator() {
                return (map, entry) -> {
                    K key = keyMapper.apply(entry);
                    U value = valueMapper.apply(entry);
                    map.merge(key, value, mergeFunction);
                };
            }

            @Override
            public BinaryOperator<Map<K, U>> combiner() {
                return (map, another) -> {
                    another.forEach((key, value) -> {
                        map.merge(key, value, mergeFunction);
                    });
                    return map;
                };
            }

            @Override
            public Function<Map<K, U>, ImmutableMap<K, U>> finisher() {
                return (map) -> {
                    ImmutableMap.Builder<K, U> builder = ImmutableMap.builder();
                    return builder.putAll(map).build();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return ImmutableSet.of();
            }
        };
    }

    public static <T, K, U> Collector<T, ?, Multimap<K, U>> toMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return new MultimapCollector<T, K, U, Multimap<K, U>>(keyMapper,
                                                              valueMapper) {
            @Override
            public Function<Multimap<K, U>, Multimap<K, U>> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
            }
        };
    }

    public static <T, K, U> Collector<T, ?, ImmutableMultimap<K, U>> toImmutableMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return new MultimapCollector<T, K, U, ImmutableMultimap<K, U>>(
                keyMapper, valueMapper) {
            @Override
            public Function<Multimap<K, U>, ImmutableMultimap<K, U>> finisher() {
                return (map) -> {
                    ImmutableMultimap.Builder<K, U> builder = ImmutableMultimap
                            .builder();
                    return builder.putAll(map).build();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return ImmutableSet.of();
            }
        };
    }
}