package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import org.springframework.stereotype.Service;

@Service
public class PayoutEventStockHandler implements EventHandler<Event> {

    @Override
    public EventAction handle(Event event, String subsKey) {
        // TODO [a.romanov]: impl
        throw new UnsupportedOperationException();
    }
}
