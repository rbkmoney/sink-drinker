package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.geck.filter.Filter;

public interface Handler<T, E> {

    void handle(T change, E event);

    Filter<T> getFilter();

    default boolean accept(T change) {
        return getFilter().match(change);
    }
}
