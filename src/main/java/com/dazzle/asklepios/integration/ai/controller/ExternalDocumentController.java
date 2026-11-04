package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.externaldocument.ProcessDocumentResponseDTO;
import com.dazzle.asklepios.integration.ai.service.ExternalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class ExternalDocumentController {

    private final ExternalDocumentService externalDocumentService;

    @PostMapping(value = "/external-documents/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProcessDocumentResponseDTO> process(
            @RequestPart("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("patientId") Long patientId,
            @RequestParam(value = "translate", defaultValue = "false") boolean translate,
            @RequestParam(value = "targetLanguage", required = false) String targetLanguage
    ) {
        return ResponseEntity.ok(
                externalDocumentService.process(file, documentType, patientId, translate, targetLanguage)
        );
    }
}
