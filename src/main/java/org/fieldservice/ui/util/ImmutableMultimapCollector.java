package org.fieldservice.ui.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ImmutableMultimapCollector<T, K, V> implements Collector<T, Builder<K, V>, ImmutableMultimap<K, V>> {

    private final Function<T, K> _keyGetter;
    private final Function<T, V> _valueGetter;

    public ImmutableMultimapCollector(Function<T, K> keyGetter, Function<T, V> valueGetter) {
        _keyGetter = keyGetter;
        _valueGetter = valueGetter;
    }

    public static <T, K, V> ImmutableMultimapCollector<T, K, V> toMultimap(Function<T, K> keyGetter,
                                                                           Function<T, V> valueGetter) {
        return new ImmutableMultimapCollector<>(keyGetter, valueGetter);
    }

    public static <T, K, V> ImmutableMultimapCollector<T, K, T> toMultimap(Function<T, K> keyGetter) {
        return new ImmutableMultimapCollector<>(keyGetter, v -> v);
    }

    @Override
    public Supplier<Builder<K, V>> supplier() {
        return Builder::new;
    }

    @Override
    public BiConsumer<Builder<K, V>, T> accumulator() {
        return (map, element) -> map.put(_keyGetter.apply(element), _valueGetter.apply(element));
    }

    @Override
    public BinaryOperator<Builder<K, V>> combiner() {
        return (map1, map2) -> {
            map1.putAll(map2.build());
            return map1;
        };
    }

    @Override
    public Function<Builder<K, V>, ImmutableMultimap<K, V>> finisher() {
        return Builder::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of();
    }
}
