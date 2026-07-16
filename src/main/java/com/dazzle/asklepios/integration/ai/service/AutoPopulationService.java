package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.integration.ai.client.AutoPopulationClient;
import com.dazzle.asklepios.integration.ai.client.dto.AutoPopulateRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.autopopulation.AutoPopulationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.autopopulation.AutoPopulationResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.autopopulation.UserContextDTO;
import com.dazzle.asklepios.integration.ai.service.mapper.EnumMapperService;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoPopulationService {

    private static final Logger LOG = LoggerFactory.getLogger(AutoPopulationService.class);

    private static final List<String> EXPECTED_OUTPUT_FIELDS = List.of(
            "chief_complaint",
            "history_of_present_illness",
            "diagnosis",
            "medications",
            "vitals",
            "procedures",
            "allergies",
            "assessment",
            "plan",
            "past_medical_history"
    );

    private final AutoPopulationClient autoPopulationClient;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final EnumMapperService enumMapperService;

    public AutoPopulationResponseDTO autoPopulate(AutoPopulateRequestDTO request) {
        LOG.debug(
                "Request to auto populate user text={} patient id={}",
                request.userText(),
                request.patientId()
        );

        Patient patient = getPatient(request.patientId());

        AutoPopulationRequestDTO payload = new AutoPopulationRequestDTO(
                UUID.randomUUID().toString(),
                buildUserContext(),
                "en",
                "en",
                request.userText(),
                buildPatientData(patient),
                EXPECTED_OUTPUT_FIELDS
        );
        LOG.debug("Auto-population request payload: {}", payload);
        return autoPopulationClient.autoPopulate(payload);
    }

    private UserContextDTO buildUserContext() {
        String login = SecurityUtils.getCurrentUserLogin().orElse("unknown");
        User user = userRepository.findByLogin(login).orElse(null);
        String role = enumMapperService.mapToAiUserRole(user != null ? user.getJobRole() : null);
        return new UserContextDTO(login, role, "general");
    }

    private Map<String, Object> buildPatientData(Patient patient) {
        Map<String, Object> patientData = new HashMap<>();
        patientData.put("medical_record_number", safe(patient.getMedicalRecordNumber()));
        patientData.put("full_name", buildFullName(patient));
        patientData.put("sex", patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : null);
        patientData.put("date_of_birth", patient.getDateOfBirth() != null
                ? patient.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                : null);
        return patientData;
    }

    private Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException("Patient not found with id " + patientId, "patient", "notfound"));
    }

    private String buildFullName(Patient patient) {
        return List.of(
                        patient.getFirstName(),
                        patient.getSecondName(),
                        patient.getThirdName(),
                        patient.getLastName()
                ).stream()
                .filter(v -> v != null && !v.isBlank())
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
