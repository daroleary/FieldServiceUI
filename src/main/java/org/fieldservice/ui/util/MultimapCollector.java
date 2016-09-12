package org.fieldservice.ui.util;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

abstract class MultimapCollector<T, K, U, R extends Multimap<K, U>>
        implements Collector<T, Multimap<K, U>, R> {
    private Function<? super T, ? extends K> _keyMapper;
    private Function<? super T, ? extends U> _valueMapper;

    MultimapCollector(Function<? super T, ? extends K> keyMapper,
                      Function<? super T, ? extends U> valueMapper) {
        _keyMapper = keyMapper;
        _valueMapper = valueMapper;
    }

    @Override
    public Supplier<Multimap<K, U>> supplier() {
        return LinkedListMultimap::create;
    }

    @Override
    public BiConsumer<Multimap<K, U>, T> accumulator() {
        return (map, data) -> map.put(_keyMapper.apply(data), _valueMapper.apply(data));
    }

    @Override
    public BinaryOperator<Multimap<K, U>> combiner() {
        return (map, another) -> {
            map.putAll(another);
            return map;
        };
    }
}
