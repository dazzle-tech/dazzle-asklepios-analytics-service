package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.externaldocument.ProcessDocumentResponseDTO;
import com.dazzle.asklepios.integration.ai.config.MedicalDocumentProcessorFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "medicalDocumentProcessorClient",
        url = "${ai.medical-document-processor.base-url}",
        configuration = MedicalDocumentProcessorFeignConfig.class
)
public interface MedicalDocumentProcessorClient {

    @PostMapping(value = "/process-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ProcessDocumentResponseDTO processDocument(
            @RequestPart("file") MultipartFile file,
            @RequestPart("document_type") String documentType,
            @RequestPart("patient") String patient,
            @RequestPart("translate") String translate,
            @RequestPart("target_language") String targetLanguage
    );
}
