package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.sinkdrinker.service.PayoutManagerEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutManagerEventHandler implements EventHandler<Event> {

    private final PayoutManagerEventService payoutManagerEventService;

    @Override
    public EventAction handle(Event event, String subsKey) {
        log.debug("Handle payout manager event with id={}", event.getId());
        try {
            payoutManagerEventService.handleEvent(event);
        } catch (Exception e) {
            log.error("Error when handling payout manager event with id={}", event.getId(), e);
            return EventAction.DELAYED_RETRY;
        }
        return EventAction.CONTINUE;
    }
}
