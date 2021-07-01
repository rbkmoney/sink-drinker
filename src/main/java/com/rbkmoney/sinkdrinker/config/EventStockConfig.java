package com.rbkmoney.sinkdrinker.config;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.sinkdrinker.handler.DamselPayoutEventHandler;
import com.rbkmoney.sinkdrinker.handler.PayoutManagerEventHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class EventStockConfig {

    @Bean
    public EventPublisher<Event> damselPayoutEventPublisher(
            DamselPayoutEventHandler damselPayoutEventHandler,
            @Value("${polling.payouter.url}") Resource resource,
            @Value("${polling.payouter.delay}") int pollDelay,
            @Value("${polling.payouter.retryDelay}") int retryDelay,
            @Value("${polling.payouter.maxPoolSize}") int maxPoolSize
    ) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(resource.getURI())
                .withPayoutServiceAdapter()
                .withEventHandler(damselPayoutEventHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .build();
    }

    @Bean
    public EventPublisher<Event> payoutManagerEventPublisher(
            PayoutManagerEventHandler payoutManagerEventHandler,
            @Value("${polling.payouter.url}") Resource resource,
            @Value("${polling.payouter.delay}") int pollDelay,
            @Value("${polling.payouter.retryDelay}") int retryDelay,
            @Value("${polling.payouter.maxPoolSize}") int maxPoolSize
    ) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(resource.getURI())
                .withPayoutServiceAdapter()
                .withEventHandler(payoutManagerEventHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .build();
    }
}
