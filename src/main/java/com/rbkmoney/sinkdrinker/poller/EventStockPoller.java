package com.rbkmoney.sinkdrinker.poller;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.SubscriberConfig;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.sinkdrinker.service.PayoutEventService;
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

    private final EventPublisher<Event> payoutEventPublisher;
    private final PayoutEventService payoutEventService;

    @Value("${polling.enabled}")
    private boolean isPollingEnabled;

    @PostConstruct
    public void subscribe() {
        if (isPollingEnabled) {
            Optional<Long> lastEventId = payoutEventService.getLastEventId();

            log.info("Subscribe to PayoutEventPublisher with lastEventId={}", lastEventId);
            payoutEventPublisher.subscribe(config(lastEventId));
        }
    }

    private SubscriberConfig<Event> config(Optional<Long> lastEventId) {
        EventConstraint.EventIDRange eventRange = new EventConstraint.EventIDRange();
        lastEventId.ifPresent(eventRange::setFromExclusive);

        return new DefaultSubscriberConfig<>(
                new EventFlowFilter<>(
                        new EventConstraint(eventRange)));
    }
}
