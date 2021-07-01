package com.rbkmoney.sinkdrinker.service;

import java.util.Optional;

public interface EventService<T> {

    void handleEvent(T event);

}
