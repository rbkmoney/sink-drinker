package com.rbkmoney.sinkdrinker.poller;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.SubscriberConfig;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.sinkdrinker.service.LastEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventStockPoller {

    private final EventPublisher<Event> damselPayoutEventPublisher;
    private final EventPublisher<Event> payoutManagerEventPublisher;
    private final LastEventService lastEventService;

    @Value("${polling.enabled}")
    private boolean isPollingEnabled;

    @Value("${last-event.sink-id.damsel-payout}")
    private String damselPayoutSinkId;

    @Value("${last-event.sink-id.payout-manager}")
    private String payoutManagerSinkId;

    @PostConstruct
    public void subscribe() {
        if (isPollingEnabled) {
            handleDamselPayoutEvent();
            handlePayoutManagerEvent();
        }
    }

    private void handleDamselPayoutEvent() {
        Optional<Long> lastEventId = lastEventService.getLastEventId(damselPayoutSinkId);
        log.info("Subscribe to DamselPayoutEventPublisher with lastEventId={}", lastEventId);
        damselPayoutEventPublisher.subscribe(config(lastEventId));
    }

    private void handlePayoutManagerEvent() {
        Optional<Long> lastEventId = lastEventService.getLastEventId(payoutManagerSinkId);
        log.info("Subscribe to PayoutManagerEventPublisher with lastEventId={}", lastEventId);
        payoutManagerEventPublisher.subscribe(config(lastEventId));
    }

    private SubscriberConfig<Event> config(Optional<Long> lastEventId) {
        EventConstraint.EventIDRange eventRange = new EventConstraint.EventIDRange();
        lastEventId.ifPresent(eventRange::setFromExclusive);

        return new DefaultSubscriberConfig<>(
                new EventFlowFilter<>(
                        new EventConstraint(eventRange)));
    }
}
