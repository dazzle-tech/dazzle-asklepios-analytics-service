package com.dazzle.asklepios.integration.ai.config;

import feign.Logger;
import feign.Request;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;

public class RecommendationsFeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options options(AiRecommendationsProperties properties) {

        int timeout =
                properties.timeoutSeconds() != null
                        ? properties.timeoutSeconds()
                        : 120;

        return new Request.Options(
                timeout,
                TimeUnit.SECONDS,
                timeout,
                TimeUnit.SECONDS,
                true
        );
    }
}