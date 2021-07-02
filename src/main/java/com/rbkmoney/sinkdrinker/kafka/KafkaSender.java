package com.rbkmoney.sinkdrinker.kafka;

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

    public void send(String topic, String key, TBase event) {
        log.info("Send event with id={} to topic={}", key, topic);
        kafkaTemplate.send(topic, key, event);
    }
}
