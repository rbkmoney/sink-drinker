package com.rbkmoney.sinkdrinker.handler;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.sinkdrinker.SinkDrinkerApplication;
import com.rbkmoney.sinkdrinker.domain.LastEvent;
import com.rbkmoney.sinkdrinker.kafka.KafkaSender;
import com.rbkmoney.sinkdrinker.repository.LastEventRepository;
import org.apache.thrift.TBase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(
        classes = {SinkDrinkerApplication.class},
        initializers = PayoutEventHandlerTest.Initializer.class)
public class PayoutEventHandlerTest {

    @Autowired
    private PayoutEventHandler payoutEventHandler;

    @Autowired
    private LastEventRepository lastEventRepository;

    @SpyBean
    private KafkaSender kafkaSender;

    @MockBean
    private KafkaTemplate<String, TBase> kafkaTemplate;

    @MockBean
    private EventPublisher<Event> payoutEventPublisher;

    @ClassRule
    @SuppressWarnings("rawtypes")
    public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:9.6")
            .withStartupTimeout(Duration.ofMinutes(5));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "spring.flyway.url=" + postgres.getJdbcUrl(),
                    "spring.flyway.user=" + postgres.getUsername(),
                    "spring.flyway.password=" + postgres.getPassword())
                    .and(configurableApplicationContext.getEnvironment().getActiveProfiles())
                    .applyTo(configurableApplicationContext);
        }
    }

    @Test
    public void shouldSendEventToKafka() {
        // Given
        Event event = new Event().setId(1L);

        // When
        payoutEventHandler.handle(event, "_");

        // Then
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId("payouter");
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(1L);

        verify(kafkaSender, only())
                .send("payout", event);
    }

    @Test
    public void shouldSendMultipleEventsToKafka() {
        for (long i = 1L; i <= 3L; i++) {
            // Given
            Event event = new Event().setId(i);

            // When
            payoutEventHandler.handle(event, "_");
        }

        // Then
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId("payouter");
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(3L);

        verify(kafkaSender, times(3))
                .send(eq("payout"), any(Event.class));
    }

    @Test
    public void shouldNotUpdateLastEventOnError() {
        doThrow(new KafkaException("fail"))
                .when(kafkaSender)
                .send("payout", new Event().setId(3L));

        for (long i = 1L; i <= 3L; i++) {
            // Given
            Event event = new Event().setId(i);

            // When
            payoutEventHandler.handle(event, "_");
        }

        // Then
        Optional<LastEvent> lastEvent = lastEventRepository.findBySinkId("payouter");
        assertThat(lastEvent).isPresent();
        assertThat(lastEvent.get().getId()).isEqualTo(2L);

        verify(kafkaSender, times(3))
                .send(eq("payout"), any(Event.class));
    }
}
