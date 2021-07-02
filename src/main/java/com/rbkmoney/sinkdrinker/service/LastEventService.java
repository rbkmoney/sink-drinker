package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.sinkdrinker.domain.LastEvent;
import com.rbkmoney.sinkdrinker.repository.LastEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LastEventService {

    private final LastEventRepository lastEventRepository;

    public void save(String sinkId, long eventId) {
        LastEvent lastEvent = new LastEvent(sinkId, eventId);
        log.info("Update lastEvent={}", lastEvent);
        lastEventRepository.save(lastEvent);
    }

    public Optional<Long> getLastEventId(String sinkId) {
        log.debug("Get LastEvent sinkId={}", sinkId);
        return lastEventRepository.findById(sinkId)
                .map(LastEvent::getId);
    }
}
