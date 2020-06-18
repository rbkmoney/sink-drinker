package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.damsel.payout_processing.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PayoutService {

    public void handleEvent(Event event) {
        // TODO [a.romanov]: write to kafka, save last event id
    }

    public Optional<Long> getLastEventId() {
        // TODO [a.romanov]: impl
        return Optional.empty();
    }
}
