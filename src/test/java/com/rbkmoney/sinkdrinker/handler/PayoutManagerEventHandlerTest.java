package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.payout.manager.PayoutPaid;
import com.rbkmoney.payout.manager.PayoutStatus;
import com.rbkmoney.sinkdrinker.kafka.KafkaSender;
import com.rbkmoney.sinkdrinker.service.LastEventService;
import com.rbkmoney.sinkdrinker.service.PartyManagementService;
import com.rbkmoney.sinkdrinker.service.PayoutSnapshotService;
import com.rbkmoney.sinkdrinker.service.ThriftEventsService;
import com.rbkmoney.testcontainers.annotations.postgresql.PostgresqlTestcontainerSingleton;
import com.rbkmoney.testcontainers.annotations.spring.boot.test.context.DefaultSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.KafkaException;

import java.util.Optional;

import static com.rbkmoney.sinkdrinker.util.DamselUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@PostgresqlTestcontainerSingleton(properties = "kafka.topic.pm-events-payout.produce.enabled=true")
@DefaultSpringBootTest
public class PayoutManagerEventHandlerTest {

    @Autowired
    private PayoutManagerEventHandler payoutManagerEventHandler;

    @Autowired
    private LastEventService lastEventService;

    @Autowired
    private PayoutSnapshotService payoutSnapshotService;

    @Autowired
    private ThriftEventsService thriftEventsService;

    @MockBean
    private KafkaSender kafkaSender;

    @MockBean
    protected PartyManagementService partyManagementService;

    @Value("${last-event.sink-id.payout-manager}")
    private String sinkId;

    @Value("${kafka.topic.pm-events-payout.name}")
    private String topicName;

    @Test
    public void shouldHandleAndSendEventToKafka() {
        String payoutId = generatePayoutId();
        when(partyManagementService.getPayoutToolId(anyString(), anyString())).thenReturn(payoutId);
        var payout = toPayoutManagerPayout(damselPayout(payoutId));
        payoutSnapshotService.save(payout, 0);
        long eventId = 1L;
        Event event = damselEvent(payoutId, eventId, damselPayoutStatusPaid());
        payoutManagerEventHandler.handle(event, "_");
        Optional<Long> lastEventId = lastEventService.getLastEventId(sinkId);
        assertThat(lastEventId).isPresent();
        assertThat(lastEventId.get()).isEqualTo(eventId);
        verify(kafkaSender, only())
                .send(eq(topicName), eq(payoutId), any(com.rbkmoney.payout.manager.Event.class));
    }

    @Test
    public void shouldSendMultipleEventsToKafka() {
        int expected = 3;
        for (int i = 1; i <= expected; i++) {
            String payoutId = String.valueOf(i);
            when(partyManagementService.getPayoutToolId(anyString(), anyString())).thenReturn(payoutId);
            Event event = damselEvent(payoutId, i, damselPayoutCreated(payoutId));
            payoutManagerEventHandler.handle(event, "_");
        }
        Optional<Long> lastEventId = lastEventService.getLastEventId(sinkId);
        assertThat(lastEventId).isPresent();
        assertThat(lastEventId.get()).isEqualTo(expected);
        verify(kafkaSender, times(expected))
                .send(eq(topicName), anyString(), any(com.rbkmoney.payout.manager.Event.class));
    }

    @Test
    public void shouldNotUpdateLastEventOnError() {
        int expectedThrowValue = 3;
        doThrow(new KafkaException("fail")).when(kafkaSender)
                .send(
                        eq(topicName),
                        eq(String.valueOf(expectedThrowValue)),
                        any(com.rbkmoney.payout.manager.Event.class));
        for (int i = 1; i <= expectedThrowValue; i++) {
            String payoutId = String.valueOf(i);
            when(partyManagementService.getPayoutToolId(anyString(), anyString())).thenReturn(payoutId);
            Event event = damselEvent(payoutId, i, damselPayoutCreated(payoutId));
            payoutManagerEventHandler.handle(event, "_");
        }
        Optional<Long> lastEventId = lastEventService.getLastEventId(sinkId);
        assertThat(lastEventId).isPresent();
        assertThat(lastEventId.get()).isEqualTo(expectedThrowValue - 1);
        verify(kafkaSender, times(expectedThrowValue))
                .send(eq(topicName), anyString(), any(com.rbkmoney.payout.manager.Event.class));
    }

    @Test
    public void shouldTryToRetryWhenPayoutNotFound() {
        String payoutId = generatePayoutId();
        long eventId = 1L;
        Event event = damselEvent(payoutId, eventId, damselPayoutStatusPaid());
        assertEquals(
                EventAction.DELAYED_RETRY,
                payoutManagerEventHandler.handle(event, "_"));
    }

    @Test
    public void shouldHandleCreatedAndSendEventToKafka() {
        String payoutId = generatePayoutId();
        when(partyManagementService.getPayoutToolId(anyString(), anyString()))
                .thenReturn(payoutId);
        long eventId = 1L;
        Event event = damselEvent(payoutId, eventId, damselPayoutCreated(payoutId));
        payoutManagerEventHandler.handle(event, "_");
        Optional<Long> lastEventId = lastEventService.getLastEventId(sinkId);
        assertThat(lastEventId).isPresent();
        assertThat(lastEventId.get()).isEqualTo(eventId);
        verify(kafkaSender, only())
                .send(eq(topicName), eq(payoutId), any(com.rbkmoney.payout.manager.Event.class));
    }

    @Test
    public void shouldSendMultiplePayoutChangesToKafka() {
        String payoutId = generatePayoutId();
        when(partyManagementService.getPayoutToolId(anyString(), anyString()))
                .thenReturn(payoutId);
        long eventId = 1L;
        Event event = damselEvent(payoutId, eventId, damselPayoutCreated(payoutId));
        payoutManagerEventHandler.handle(event, "_");
        Optional<Long> lastEventId = lastEventService.getLastEventId(sinkId);
        assertThat(lastEventId).isPresent();
        assertThat(lastEventId.get()).isEqualTo(eventId);
        eventId = 2L;
        event = damselEvent(payoutId, eventId, damselPayoutStatusPaid());
        payoutManagerEventHandler.handle(event, "_");
        lastEventId = lastEventService.getLastEventId(sinkId);
        assertThat(lastEventId).isPresent();
        assertThat(lastEventId.get()).isEqualTo(eventId);
        assertEquals(1, payoutSnapshotService.get(payoutId).getSequenceId());
        assertEquals(PayoutStatus.paid(new PayoutPaid()), payoutSnapshotService.get(payoutId).getPayout().getStatus());
        verify(kafkaSender, times(2))
                .send(eq(topicName), eq(payoutId), any(com.rbkmoney.payout.manager.Event.class));
    }
}
