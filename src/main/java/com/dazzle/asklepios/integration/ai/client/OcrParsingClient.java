package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.ExtractAndParseResponseDTO;
import com.dazzle.asklepios.integration.ai.config.OcrParsingFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "ocrParsingClient",
        url = "http://localhost:8080",
        configuration = OcrParsingFeignConfig.class
)
public interface OcrParsingClient {

    @PostMapping(value = "/api/ai/v1/ocr-parsing-service/extract-and-parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ExtractAndParseResponseDTO extractAndParse(@RequestPart("file") MultipartFile file);
}
