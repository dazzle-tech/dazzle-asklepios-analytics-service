package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.timeline.TimelineRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.TimelineResponseDTO;
import com.dazzle.asklepios.integration.ai.config.PatientTimelineFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "patientTimelineClient",
        url = "${ai.patient-timeline.base-url}",
        configuration = PatientTimelineFeignConfig.class
)
public interface PatientTimelineClient {

    @PostMapping("/generate-timeline")
    TimelineResponseDTO generateTimeline(@RequestBody TimelineRequestDTO request);
}
