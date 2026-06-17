package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.OCRParsingResponseDTO;
import com.dazzle.asklepios.integration.ai.service.OcrParsingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class OcrParsingController {

    private final OcrParsingService ocrParsingService;

    @PostMapping(value = "/ocr/extract-and-parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OCRParsingResponseDTO
            > extractAndParse(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ocrParsingService.extractAndParse(file));
    }
}
