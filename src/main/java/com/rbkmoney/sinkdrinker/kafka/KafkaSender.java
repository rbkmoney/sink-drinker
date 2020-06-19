package com.rbkmoney.sinkdrinker.kafka;

import com.rbkmoney.damsel.payout_processing.Event;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaSender {

    private final KafkaTemplate<String, TBase> kafkaTemplate;

    public long send(String topic, Event event) {
        String key = String.valueOf(event.getId());
        kafkaTemplate.send(topic, key, event);
        return event.getId();
    }
}
