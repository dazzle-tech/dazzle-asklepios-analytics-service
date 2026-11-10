package com.dazzle.asklepios.integration.ai.config;

import feign.Logger;
import feign.Request;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class MedicalDocumentProcessorFeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder();
    }

    @Bean
    public Request.Options options(MedicalDocumentProcessorProperties properties) {
        int timeout = properties.timeoutSeconds() != null ? properties.timeoutSeconds() : 360;
        return new Request.Options(timeout, TimeUnit.SECONDS, timeout, TimeUnit.SECONDS, true);
    }
}
