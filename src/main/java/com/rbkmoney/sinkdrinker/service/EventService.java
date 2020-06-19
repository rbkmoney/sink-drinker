package com.rbkmoney.sinkdrinker.service;

import java.util.Optional;

public interface EventService<TEvent> {

    void handleEvent(TEvent event);

    Optional<Long> getLastEventId();
}
