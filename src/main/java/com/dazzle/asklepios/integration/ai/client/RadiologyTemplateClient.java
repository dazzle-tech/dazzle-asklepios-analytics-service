package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.radiology.SelectAndFillRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.radiology.SelectAndFillResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "radiologyTemplateClient",
        url = "${ai.radiology-template.base-url}"
)
public interface RadiologyTemplateClient {


    @PostMapping("/select-and-fill")
    SelectAndFillResponseDTO selectAndFill(
            @RequestBody SelectAndFillRequestDTO request
    );
}