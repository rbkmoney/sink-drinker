package com.rbkmoney.sinkdrinker.config;

import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.sinkdrinker.handler.PayoutEventStockHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class EventStockConfig {

    @Bean
    public EventPublisher<Event> payoutEventPublisher(
            PayoutEventStockHandler payoutEventStockHandler,
            @Value("${polling.payouter.url}") Resource resource,
            @Value("${polling.payouter.delay}") int pollDelay,
            @Value("${polling.payouter.retryDelay}") int retryDelay,
            @Value("${polling.payouter.maxPoolSize}") int maxPoolSize
    ) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(resource.getURI())
                .withPayoutServiceAdapter()
                .withEventHandler(payoutEventStockHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .build();
    }
}
