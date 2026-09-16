package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.integration.ai.client.OcrParsingClient;
import com.dazzle.asklepios.integration.ai.client.dto.ExtractAndParseResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.OCRParsingResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.PatientInfoResponseDTO;
import com.dazzle.asklepios.integration.ai.service.mapper.ApLovMapperService;
import com.dazzle.asklepios.integration.ai.service.mapper.DateMapperService;
import com.dazzle.asklepios.integration.ai.service.mapper.EnumMapperService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class OcrParsingService {

    private static final Logger LOG = LoggerFactory.getLogger(OcrParsingService.class);

    private final OcrParsingClient ocrParsingClient;
    private final ApLovMapperService apLovMapperService;
    private final EnumMapperService enumMapperService;
    private final DateMapperService dateMapperService;
public PatientInfoResponseDTO extractAndParse(MultipartFile file) {
    LOG.debug("[OCR] extract-and-parse request fileName={} size={}",
            file.getOriginalFilename(), file.getSize());

    ExtractAndParseResponseDTO response = ocrParsingClient.extractAndParse(file);

    OCRParsingResponseDTO original =  response.structuredData();

    String key = apLovMapperService.mapNationalityToKey(
            original.nationality()
    );

    Gender gender = enumMapperService.mapToGender(original.sex());

    LocalDate dob = dateMapperService.parseDateOfBirth(original.dateOfBirth());

    PatientInfoResponseDTO patientInfoResponseDTO = new PatientInfoResponseDTO(
            original.type(),
            original.documentNumber(),
            original.familyName(),
            original.givenNames(),
            key,
            dob,
            gender,
            original.placeOfBirth()
    );

    return patientInfoResponseDTO;
}
}
