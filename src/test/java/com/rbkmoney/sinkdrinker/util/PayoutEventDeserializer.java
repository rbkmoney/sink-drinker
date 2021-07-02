package com.rbkmoney.sinkdrinker.util;

import com.rbkmoney.kafka.common.serialization.AbstractThriftDeserializer;
import com.rbkmoney.payout.manager.Event;

public class PayoutEventDeserializer extends AbstractThriftDeserializer<Event> {

    @Override
    public Event deserialize(String s, byte[] bytes) {
        return super.deserialize(bytes, new Event());
    }
}
