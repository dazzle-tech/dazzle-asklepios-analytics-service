package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.integration.ai.client.MedicalDocumentProcessorClient;
import com.dazzle.asklepios.integration.ai.client.dto.externaldocument.DocumentPatientDTO;
import com.dazzle.asklepios.integration.ai.client.dto.externaldocument.ProcessDocumentResponseDTO;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class ExternalDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalDocumentService.class);
    private static final String ENTITY_NAME = "externalDocument";

    private final MedicalDocumentProcessorClient medicalDocumentProcessorClient;
    private final PatientRepository patientRepository;
    private final JsonMapper jsonMapper;

    public ProcessDocumentResponseDTO process(
            MultipartFile file,
            String documentType,
            Long patientId,
            boolean translate,
            String targetLanguage
    ) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestAlertException("File is required", ENTITY_NAME, "file.required");
        }

        String aiDocumentType = toAiDocumentType(documentType);
        // Patient identity comes from our DB, never from the client, so the AI matches against the real record
        String patientJson = toJson(buildPatient(getPatient(patientId)));

        LOG.debug("[EXTERNAL-DOC] process fileName={} size={} type={} patientId={}",
                file.getOriginalFilename(), file.getSize(), aiDocumentType, patientId);

        return medicalDocumentProcessorClient.processDocument(
                file,
                aiDocumentType,
                patientJson,
                String.valueOf(translate),
                targetLanguage == null || targetLanguage.isBlank() ? "en" : targetLanguage
        );
    }

    private String toAiDocumentType(String documentType) {
        String type = documentType == null ? "" : documentType.trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "LAB" -> "lab_result";
            case "RADIOLOGY" -> "radiology_report";
            default -> throw new BadRequestAlertException(
                    "Document type must be LAB or RADIOLOGY", ENTITY_NAME, "documenttype.invalid");
        };
    }

    private Patient getPatient(Long patientId) {
        if (patientId == null) {
            throw new BadRequestAlertException("Patient is required", ENTITY_NAME, "patient.required");
        }
        return patientRepository
                .findById(patientId)
                .orElseThrow(() -> new BadRequestAlertException("Patient not found", "patient", "notfound"));
    }

    private DocumentPatientDTO buildPatient(Patient patient) {
        String fullName = Stream.of(patient.getFirstName(), patient.getLastName())
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" "));

        return new DocumentPatientDTO(
                String.valueOf(patient.getId()),
                fullName.isBlank() ? null : fullName,
                toAiSex(patient.getSexAtBirth()),
                toIsoDate(patient.getDateOfBirth())
        );
    }

    // The AI accepts only male / female / unspecified
    private String toAiSex(String sexAtBirth) {
        String sex = sexAtBirth == null ? "" : sexAtBirth.trim().toLowerCase(Locale.ROOT);
        if (sex.startsWith("m")) return "male";
        if (sex.startsWith("f")) return "female";
        return "unspecified";
    }

    private String toIsoDate(Date date) {
        if (date == null) return null;
        // java.sql.Date does not support toInstant()
        if (date instanceof java.sql.Date sqlDate) return sqlDate.toLocalDate().toString();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    private String toJson(DocumentPatientDTO patient) {
        return jsonMapper.writeValueAsString(patient);
    }
}
