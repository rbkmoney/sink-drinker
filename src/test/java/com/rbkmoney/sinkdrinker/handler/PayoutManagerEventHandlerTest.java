package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.damsel.payout_processing.*;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.sinkdrinker.config.AbstractDaoConfig;
import com.rbkmoney.sinkdrinker.domain.LastEvent;
import com.rbkmoney.sinkdrinker.domain.SequenceForPayout;
import com.rbkmoney.sinkdrinker.kafka.KafkaSender;
import com.rbkmoney.sinkdrinker.repository.LastEventRepository;
import com.rbkmoney.sinkdrinker.repository.SequenceForPayoutRepository;
import com.rbkmoney.sinkdrinker.service.PartyManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.KafkaException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "kafka.topic.pm-events-payout.produce.enabled=true"})
public class PayoutManagerEventHandlerTest extends AbstractDaoConfig {

    @Autowired
    private PayoutManagerEventHandler payoutManagerEventHandler;

    @Autowired
    private LastEventRepository lastEventRepository;

    @Autowired
    private SequenceForPayoutRepository sequenceForPayoutRepository;

    @MockBean
    private KafkaSender kafkaSender;

    @MockBean
    private PartyManagementService partyManagementService;

    @Value("${last-event.sink-id.payout-manager}")
    private String sinkId;

    @Value("${kafka.topic.pm-events-payout.name}")
    private String topicName;

    @Test
    public void shouldHandleAndSendEventToKafka() {
        String payoutId = generatePayoutId();
        sequenceForPayoutRepository.save(new SequenceForPayout(payoutId, 0));
        long eventId = 1L;
        Event event = damselEvent(payoutId, eventId, damselPayoutStatusPaid());
        payoutManagerEventHandler.handle(event, "_");
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(eventId);
        verify(kafkaSender, only())
                .send(eq(topicName), eq(payoutId), any(com.rbkmoney.payout.manager.Event.class));
    }

    @Test
    public void shouldSendMultipleEventsToKafka() {
        int expected = 3;
        for (int i = 1; i <= expected; i++) {
            sequenceForPayoutRepository.save(new SequenceForPayout(String.valueOf(i), 0));
            Event event = damselEvent(String.valueOf(i), i, damselPayoutStatusPaid());
            payoutManagerEventHandler.handle(event, "_");
        }
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(expected);
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
            sequenceForPayoutRepository.save(new SequenceForPayout(String.valueOf(i), 0));
            Event event = damselEvent(String.valueOf(i), i, damselPayoutStatusPaid());
            payoutManagerEventHandler.handle(event, "_");
        }

        // Then
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(expectedThrowValue - 1);

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
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(eventId);
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
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(eventId);
        eventId = 2L;
        event = damselEvent(payoutId, eventId, damselPayoutStatusPaid());
        payoutManagerEventHandler.handle(event, "_");
        lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(eventId);
        verify(kafkaSender, times(2))
                .send(eq(topicName), eq(payoutId), any(com.rbkmoney.payout.manager.Event.class));
    }
}
