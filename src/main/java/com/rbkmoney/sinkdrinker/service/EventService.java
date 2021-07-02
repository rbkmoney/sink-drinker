package com.rbkmoney.sinkdrinker.service;

public interface EventService<T> {

    void handleEvent(T event);

}
