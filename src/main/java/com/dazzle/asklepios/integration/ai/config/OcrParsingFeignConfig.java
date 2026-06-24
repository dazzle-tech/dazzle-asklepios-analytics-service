package com.dazzle.asklepios.integration.ai.config;

import feign.Logger;
import feign.Request;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;

public class OcrParsingFeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder();
    }

    @Bean
    public Request.Options options(AiOcrParsingProperties properties) {
//        int timeout = properties.timeoutSeconds() != null ? properties.timeoutSeconds() : 3600;
        int timeout = 3600;
        return new Request.Options(timeout, TimeUnit.SECONDS, timeout, TimeUnit.SECONDS, true);
    }
}
