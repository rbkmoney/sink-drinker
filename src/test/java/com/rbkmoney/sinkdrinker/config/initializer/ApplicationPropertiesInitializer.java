package com.rbkmoney.sinkdrinker.config.initializer;

import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;

public class ApplicationPropertiesInitializer extends ConfigDataApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of("polling.enabled=false")
                .applyTo(configurableApplicationContext);
    }
}
