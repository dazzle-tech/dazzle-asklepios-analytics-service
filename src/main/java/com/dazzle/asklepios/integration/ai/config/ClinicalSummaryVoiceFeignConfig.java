package com.dazzle.asklepios.integration.ai.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class ClinicalSummaryVoiceFeignConfig {

    @Bean
    Logger.Level clinicalSummaryFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options clinicalSummaryRequestOptions(
            ClinicalSummaryVoiceProperties properties
    ) {
        int timeout = properties.timeoutSeconds() != null
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