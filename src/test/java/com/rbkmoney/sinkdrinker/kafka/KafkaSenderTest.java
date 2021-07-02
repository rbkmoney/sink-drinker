package com.rbkmoney.sinkdrinker.kafka;

import com.rbkmoney.payout.manager.Event;
import com.rbkmoney.sinkdrinker.config.AbstractKafkaConfig;
import com.rbkmoney.sinkdrinker.service.ThriftUtil;
import com.rbkmoney.sinkdrinker.util.PayoutEventDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaSenderTest extends AbstractKafkaConfig {

    @Value("${kafka.topic.pm-events-payout.name}")
    private String topicName;

    @Autowired
    private KafkaSender kafkaSender;

    @Test
    public void shouldProduceEvents() {
        int expected = 4;
        long eventId = 1L;
        for (int i = 1; i <= expected; i++) {
            String payoutId = String.valueOf(i);
            var damselEvent = damselEvent(payoutId, eventId, damselPayoutCreated(payoutId));
            Event event = ThriftUtil.createEvents(
                    damselEvent,
                    payoutId,
                    (id, isCreatedEvent) -> 0,
                    (partyId, shopId) -> payoutId)
                    .get(0);
            kafkaSender.send(topicName, payoutId, event);
        }
        Consumer<String, Event> consumer = createConsumer(PayoutEventDeserializer.class);
        consumer.subscribe(List.of(topicName));
        ConsumerRecords<String, Event> poll = consumer.poll(Duration.ofMillis(5000));
        assertEquals(expected, poll.count());
        Iterable<ConsumerRecord<String, Event>> records = poll.records(topicName);
        List<Event> events = new ArrayList<>();
        records.forEach(consumerRecord -> events.add(consumerRecord.value()));
        for (int i = 0; i < expected; i++) {
            assertEquals(String.valueOf(i), events.get(i).getPayoutId());
        }
    }
}