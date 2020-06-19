package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.sinkdrinker.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutEventStockHandler implements EventHandler<Event> {

    private final PayoutService payoutService;

    @Override
    public EventAction handle(Event event, String subsKey) {
        try {
            payoutService.handleEvent(event);
        } catch (Exception e) {
            log.error("Error when handling payout event with id={}", event.getId(), e);
            return EventAction.DELAYED_RETRY;
        }

        return EventAction.CONTINUE;
    }
}
