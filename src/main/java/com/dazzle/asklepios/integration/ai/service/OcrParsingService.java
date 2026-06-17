package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.integration.ai.client.OcrParsingClient;
import com.dazzle.asklepios.integration.ai.client.dto.ExtractAndParseResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.OCRParsingResponseDTO;
import com.dazzle.asklepios.integration.ai.service.mapper.ApLovMapperService;
import com.dazzle.asklepios.integration.ai.service.mapper.AsklepiosLovCodes;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OcrParsingService {

    private static final Logger LOG = LoggerFactory.getLogger(OcrParsingService.class);

    private final OcrParsingClient ocrParsingClient;
    private final ApLovMapperService apLovMapperService;

//    public OCRParsingResponseDTO extractAndParse(MultipartFile file) {
//        LOG.debug("[OCR] extract-and-parse request fileName={} size={}", file.getOriginalFilename(), file.getSize());
//        ExtractAndParseResponseDTO extractAndParseResponseDTO = ocrParsingClient.extractAndParse(file);
//       ٍString code =  apLovMapperService.getCleanValueCodeByLovCodeAndKey(
//                AsklepiosLovCodes.NATIONALITY,
//                extractAndParseResponseDTO.structuredData().nationality()
//        );
//        ExtractAndParseResponseDTO extractAndParseResponseDTO2 =
//        return extractAndParseResponseDTO.structuredData();
//    }
public OCRParsingResponseDTO extractAndParse(MultipartFile file) {
    LOG.debug("[OCR] extract-and-parse request fileName={} size={}",
            file.getOriginalFilename(), file.getSize());

    ExtractAndParseResponseDTO response = ocrParsingClient.extractAndParse(file);

    OCRParsingResponseDTO original =  response.structuredData();

    String code = apLovMapperService.getCleanValueCodeByLovCodeAndKey(
            AsklepiosLovCodes.NATIONALITY,
            original.nationality()
    );

    OCRParsingResponseDTO updatedStructuredData = new OCRParsingResponseDTO(
            original.type(),
            original.documentNumber(),
            original.familyName(),
            original.givenNames(),
            code,
            original.dateOfBirth(),
            original.sex(),
            original.placeOfBirth()// حسب DTO عندك
    );

    return updatedStructuredData;
}
}
