package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.config.ConvoVoiceAgentFeignConfig;
import tools.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
        name = "convoVoiceAgentClient",
        url = "${ai.convo-voice-agent.base-url}",
        configuration = ConvoVoiceAgentFeignConfig.class
)
public interface ConvoVoiceAgentClient {

    @PostMapping(
            value = "/draft",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    JsonNode draft(
            @RequestHeader(value = "X-Clinician-Id", required = false) String clinicianId,
            Map<String, ?> form
    );

    @PostMapping(
            value = "/sections/regenerate",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    JsonNode regenerate(
            @RequestHeader(value = "X-Clinician-Id", required = false) String clinicianId,
            @RequestBody String body
    );
}
