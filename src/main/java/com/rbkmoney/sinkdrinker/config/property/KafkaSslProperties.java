package com.rbkmoney.sinkdrinker.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.ssl")
public class KafkaSslProperties {

    private boolean enabled;

    private String trustStoreType;
    private String trustStoreLocation;
    private String trustStorePassword;

    private String keyStoreType;
    private String keyStoreLocation;
    private String keyStorePassword;

    private String keyPassword;
}
