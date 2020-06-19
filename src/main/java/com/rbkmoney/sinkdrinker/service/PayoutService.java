package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.sinkdrinker.kafka.KafkaSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final KafkaSender kafkaSender;

    @Value("${kafka.topic.payouts}")
    private String payoutsKafkaTopic;

    public void handleEvent(Event event) {
        String eventId = kafkaSender.send(payoutsKafkaTopic, event);
        // TODO [a.romanov]: save eventId
    }

    public Optional<Long> getLastEventId() {
        // TODO [a.romanov]: impl
        return Optional.empty();
    }
}
