package com.rbkmoney.sinkdrinker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class SinkDrinkerApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(SinkDrinkerApplication.class, args);
    }
}
