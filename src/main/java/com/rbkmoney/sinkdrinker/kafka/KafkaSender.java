package com.rbkmoney.sinkdrinker.kafka;

import com.rbkmoney.damsel.payout_processing.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSender {

    private final KafkaTemplate<String, TBase> kafkaTemplate;

    public long send(String topic, Event event) {
        String key = event.getSource().getPayoutId();

        log.debug("Send event with id={} to topic={}", event.getId(), topic);
        kafkaTemplate.send(topic, key, event);
        return event.getId();
    }
}
