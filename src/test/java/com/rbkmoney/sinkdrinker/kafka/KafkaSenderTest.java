package com.rbkmoney.sinkdrinker.kafka;

import com.rbkmoney.payout.manager.Event;
import com.rbkmoney.sinkdrinker.service.PartyManagementService;
import com.rbkmoney.sinkdrinker.service.ThriftEventsService;
import com.rbkmoney.testcontainers.annotations.KafkaSpringBootTest;
import com.rbkmoney.testcontainers.annotations.kafka.KafkaTestcontainer;
import com.rbkmoney.testcontainers.annotations.kafka.config.KafkaConsumer;
import com.rbkmoney.testcontainers.annotations.postgresql.PostgresqlTestcontainerSingleton;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.rbkmoney.sinkdrinker.util.DamselUtil.damselEvent;
import static com.rbkmoney.sinkdrinker.util.DamselUtil.damselPayoutCreated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@PostgresqlTestcontainerSingleton
@KafkaTestcontainer(
        properties = "kafka.topic.pm-events-payout.produce.enabled=true",
        topicsKeys = "kafka.topic.pm-events-payout.name")
@KafkaSpringBootTest
@Disabled
public class KafkaSenderTest {

    private static final int TIMEOUT = 5;

    @Value("${kafka.topic.pm-events-payout.name}")
    private String topicName;

    @MockBean
    private PartyManagementService partyManagementService;

    @Autowired
    private KafkaSender kafkaSender;

    @Autowired
    private KafkaConsumer<Event> testPayoutEventKafkaConsumer;

    @Autowired
    private ThriftEventsService thriftEventsService;

    @Test
    public void shouldProduceEvents() {
        int expected = 2;
        long eventId = 1L;
        sendEventInTopic(expected, eventId);
        List<Event> readEvents = new ArrayList<>();
        testPayoutEventKafkaConsumer.read(topicName, data -> readEvents.add(data.value()));
        Unreliables.retryUntilTrue(TIMEOUT, TimeUnit.SECONDS, () -> readEvents.size() == expected);
        for (int i = 0; i < expected; i++) {
            Integer id = i + 1;
            assertEquals(String.valueOf(id), readEvents.get(i).getPayoutId());
        }
    }

    private void sendEventInTopic(int expected, long eventId) {
        for (int i = 1; i <= expected; i++) {
            String payoutId = String.valueOf(i);
            reset(partyManagementService);
            when(partyManagementService.getPayoutToolId(anyString(), anyString()))
                    .thenReturn(payoutId);
            var damselEvent = damselEvent(payoutId, eventId, damselPayoutCreated(payoutId));
            Event event = thriftEventsService.createEvents(damselEvent, payoutId).get(0);
            kafkaSender.send(topicName, payoutId, event);
        }
    }
}
