package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.sinkdrinker.domain.LastEvent;
import com.rbkmoney.sinkdrinker.kafka.KafkaSender;
import com.rbkmoney.sinkdrinker.repository.LastEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final KafkaSender kafkaSender;
    private final LastEventRepository lastEventRepository;

    @Value("${polling.payouter.sink-id}")
    private String payouterSinkId;

    @Value("${kafka.topic.payouts}")
    private String payoutsKafkaTopic;

    public void handleEvent(Event event) {
        long eventId = kafkaSender.send(payoutsKafkaTopic, event);
        lastEventRepository.save(new LastEvent(payouterSinkId, eventId));
    }

    public Optional<Long> getLastEventId() {
        return lastEventRepository.findBySinkId(payouterSinkId)
                .map(LastEvent::getId);
    }
}
