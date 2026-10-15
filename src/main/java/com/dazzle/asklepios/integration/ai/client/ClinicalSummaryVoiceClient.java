package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.clinicalsummaryvoice.ClinicalSummaryVoiceResponseDTO;
import com.dazzle.asklepios.integration.ai.config.ClinicalSummaryVoiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "clinicalSummaryVoiceClient",
        url = "${ai.clinical-summary-voice.base-url}",
        configuration = ClinicalSummaryVoiceFeignConfig.class
)
public interface ClinicalSummaryVoiceClient {

    @PostMapping(
            value = "/summarize",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ClinicalSummaryVoiceResponseDTO summarize(
            @RequestPart("audio") MultipartFile audio
    );
}