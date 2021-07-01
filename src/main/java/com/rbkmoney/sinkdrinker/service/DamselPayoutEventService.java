package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.sinkdrinker.kafka.KafkaSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DamselPayoutEventService implements EventService<Event> {

    private final KafkaSender kafkaSender;
    private final LastEventService lastEventService;

    @Value("${last-event.sink-id.damsel-payout}")
    private String sinkId;

    @Value("${kafka.topic.payout}")
    private String topicName;

    @Override
    public void handleEvent(Event event) {
        String payoutId = event.getSource().getPayoutId();
        kafkaSender.send(topicName, payoutId, event);
        lastEventService.save(sinkId, event.getId());
    }
}
