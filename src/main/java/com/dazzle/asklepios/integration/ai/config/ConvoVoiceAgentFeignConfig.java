package com.dazzle.asklepios.integration.ai.config;

import feign.Logger;
import feign.Request;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class ConvoVoiceAgentFeignConfig {

    @Bean
    Logger.Level convoVoiceAgentFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options convoVoiceAgentRequestOptions(ConvoVoiceAgentProperties properties) {
        int timeout = properties.timeoutSeconds() != null
                ? properties.timeoutSeconds()
                : 300;

        return new Request.Options(
                timeout,
                TimeUnit.SECONDS,
                timeout,
                TimeUnit.SECONDS,
                true
        );
    }

    @Bean
    public Encoder convoVoiceAgentFormEncoder() {
        return new SpringFormEncoder();
    }
}
