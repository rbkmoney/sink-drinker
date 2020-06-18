package com.rbkmoney.sinkdrinker.poller;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.SubscriberConfig;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventStockPoller {

    private final EventPublisher<Event> payoutEventPublisher;

    @Value("${polling.enabled}")
    private boolean isPollingEnabled;

    @PostConstruct
    public void subscribe() {
        if (isPollingEnabled) {
            // TODO [a.romanov]: last event id
            payoutEventPublisher.subscribe(subscriberConfig(Optional.empty()));
        }
    }

    private SubscriberConfig<Event> subscriberConfig(Optional<Long> lastEventId) {
        EventConstraint.EventIDRange eventRange = new EventConstraint.EventIDRange();
        lastEventId.ifPresent(eventRange::setFromExclusive);

        return new DefaultSubscriberConfig<>(
                new EventFlowFilter<>(
                        new EventConstraint(eventRange)));
    }
}
