package com.dazzle.asklepios.integration.ai.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class AutoPopulationFeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options options(AutoPopulationProperties properties) {
        int timeout = properties.timeoutSeconds() != null ? properties.timeoutSeconds() : 60;
        return new Request.Options(timeout, TimeUnit.SECONDS, timeout, TimeUnit.SECONDS, true);
    }
}
