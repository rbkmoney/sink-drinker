package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.sinkdrinker.kafka.KafkaSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutManagerEventService implements EventService<Event> {

    private final PartyManagementService partyManagementService;
    private final SequenceForPayoutService sequenceForPayoutService;
    private final LastEventService lastEventService;
    private final KafkaSender kafkaSender;

    @Value("${last-event.sink-id.payout-manager}")
    private String sinkId;

    @Value("${kafka.topic.pm-events-payout.name}")
    private String topicName;

    @Value("${kafka.topic.pm-events-payout.produce.enabled}")
    private boolean producerEnabled;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvent(Event damselEvent) {
        String payoutId = damselEvent.getSource().getPayoutId();
        if (producerEnabled) {
            var events = ThriftUtil.createEvents(
                    damselEvent,
                    payoutId,
                    sequenceForPayoutService::getSequenceId,
                    partyManagementService::getPayoutToolId);
            for (var event : events) {
                kafkaSender.send(topicName, payoutId, event);
            }
        }
        lastEventService.save(sinkId, damselEvent.getId());
    }
}
