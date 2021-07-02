package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.sinkdrinker.service.DamselPayoutEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DamselPayoutEventHandler implements EventHandler<Event> {

    private final DamselPayoutEventService damselPayoutEventService;

    @Override
    public EventAction handle(Event event, String subsKey) {
        log.debug("Handle damsel payout event with id={}", event.getId());

        try {
            damselPayoutEventService.handleEvent(event);
        } catch (Exception e) {
            log.error("Error when handling damsel payout event with id={}", event.getId(), e);
            return EventAction.DELAYED_RETRY;
        }

        return EventAction.CONTINUE;
    }
}
