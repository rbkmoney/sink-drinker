package com.rbkmoney.sinkdrinker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.geck.serializer.kit.json.JsonProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class ApplicationConfig {

    @Bean
    public PartyManagementSrv.Iface partyManagementClient(
            @Value("${service.partyManagement.url}") Resource resource,
            @Value("${service.partyManagement.networkTimeout}") int networkTimeout
    ) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(PartyManagementSrv.Iface.class);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules();
    }

    @Bean
    public TBaseProcessor thriftBaseProcessor() {
        return new TBaseProcessor();
    }

    @Bean
    public JsonProcessor jsonProcessor() {
        return new JsonProcessor();
    }
}
