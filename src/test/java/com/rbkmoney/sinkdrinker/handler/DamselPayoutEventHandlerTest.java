package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.damsel.payout_processing.EventSource;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.sinkdrinker.config.AbstractDaoConfig;
import com.rbkmoney.sinkdrinker.domain.LastEvent;
import com.rbkmoney.sinkdrinker.kafka.KafkaSender;
import com.rbkmoney.sinkdrinker.repository.LastEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.KafkaException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DamselPayoutEventHandlerTest extends AbstractDaoConfig {

    @Autowired
    private DamselPayoutEventHandler damselPayoutEventHandler;

    @Autowired
    private LastEventRepository lastEventRepository;

    @MockBean
    private KafkaSender kafkaSender;

    @MockBean
    private EventPublisher<Event> payoutEventPublisher;

    @Value("${last-event.sink-id.damsel-payout}")
    private String sinkId;

    @Test
    public void shouldSendEventToKafka() {
        // Given
        Event event = new Event().setId(1L).setSource(EventSource.payout_id("payout_id"));

        // When
        damselPayoutEventHandler.handle(event, "_");

        // Then
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(1L);

        verify(kafkaSender, only())
                .send("payout", "payout_id", event);
    }

    @Test
    public void shouldSendMultipleEventsToKafka() {
        for (long i = 1L; i <= 3L; i++) {
            // Given
            Event event = new Event().setId(i).setSource(EventSource.payout_id(String.valueOf(i)));

            // When
            damselPayoutEventHandler.handle(event, "_");
        }

        // Then
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(3L);

        verify(kafkaSender, times(3))
                .send(eq("payout"), any(), any(Event.class));
    }

    @Test
    public void shouldNotUpdateLastEventOnError() {
        String payoutId = "3";
        doThrow(new KafkaException("fail"))
                .when(kafkaSender)
                .send("payout", payoutId, new Event().setId(3L).setSource(EventSource.payout_id(payoutId)));

        for (long i = 1L; i <= 3L; i++) {
            // Given
            Event event = new Event().setId(i).setSource(EventSource.payout_id(String.valueOf(i)));

            // When
            damselPayoutEventHandler.handle(event, "_");
        }

        // Then
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId(sinkId);
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(2L);

        verify(kafkaSender, times(3))
                .send(eq("payout"), any(), any(Event.class));
    }
}
