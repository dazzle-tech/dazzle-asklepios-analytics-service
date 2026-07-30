package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.BodyMeasurements;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.integration.ai.client.SepsisEarlyDetectionClient;
import com.dazzle.asklepios.integration.ai.client.dto.sepsis.SepsisPatientDataDTO;
import com.dazzle.asklepios.integration.ai.client.dto.sepsis.SepsisPatientInfoDTO;
import com.dazzle.asklepios.integration.ai.client.dto.sepsis.SepsisRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.sepsis.SepsisResponseDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.SepsisRequestVM;
import com.dazzle.asklepios.repository.BodyMeasurementsRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryBodyMeasurementsDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SepsisEarlyDetectionAiService {

    private static final Logger LOG = LoggerFactory.getLogger(SepsisEarlyDetectionAiService.class);

    private final PatientRepository patientRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final PatientAllergyRepository allergyRepository;
    private final SepsisEarlyDetectionClient client;
    private final BodyMeasurementsRepository bodyMeasurementsRepository;

    public SepsisResponseDTO analyse(SepsisRequestVM request) {

        SepsisRequestDTO aiRequest = buildRequest(request);

        LOG.debug("Calling Sepsis Early Detection AI");
        LOG.debug("Sepsis request={}", aiRequest);
        SepsisResponseDTO analysis= client.analysis(aiRequest);
        return analysis;
    }

    public SepsisRequestDTO buildRequest(SepsisRequestVM request) {

        validate(request);

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() ->
                        new BadRequestAlertException(
                                "Patient not found",
                                "patient",
                                "notfound"));

        return new SepsisRequestDTO(
                new SepsisPatientDataDTO(
                        buildPatientInfo(patient),
                        request.hourlyData()
                )
        );
    }

    private void validate(SepsisRequestVM request) {

        if (request.patientId() == null) {
            throw new BadRequestAlertException(
                    "Patient is required",
                    "sepsis",
                    "patient_required");
        }
    }

    private SepsisPatientInfoDTO buildPatientInfo(Patient patient) {
        NurseSummaryBodyMeasurementsDTO bodyMeasurementsDto = null;
        if (patient != null) {
            Optional<BodyMeasurements> latestBody =
                    bodyMeasurementsRepository.findFirstByPatientIdAndIsActiveTrueOrderByCreatedDateDesc(patient.getId());

            if (latestBody.isPresent()) {
                com.dazzle.asklepios.domain.BodyMeasurements bm = latestBody.get();
                bodyMeasurementsDto = new NurseSummaryBodyMeasurementsDTO(
                        bm.getWeight(),
                        bm.getHeight(),
                        bm.getHeadCircumference()
                );
            }
        }
        return new SepsisPatientInfoDTO(
                patient.getFirstName() + patient.getLastName(),
                calculateAge(patient),
                patient.getSexAtBirth() != null
                        ? patient.getSexAtBirth().name()
                        : null,
                bodyMeasurementsDto != null ? bodyMeasurementsDto.weight() : null,
                bodyMeasurementsDto != null ? bodyMeasurementsDto.height() : null,
                calculateBmi(patient),
                patient.getBloodGroup(),
                resolveAdmissionReason(patient),
                getComorbidities(patient.getId()),
                getAllergies(patient.getId())
        );
    }

    private Integer calculateAge(Patient patient) {

        if (patient.getDateOfBirth() == null) {
            return null;
        }

        return Period.between(
                        patient.getDateOfBirth()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate(),
                        LocalDate.now())
                .getYears();
    }

    private BigDecimal calculateBmi(Patient patient) {
        NurseSummaryBodyMeasurementsDTO bodyMeasurementsDto = null;
        if (patient != null) {
            Optional<BodyMeasurements> latestBody =
                    bodyMeasurementsRepository.findFirstByPatientIdAndIsActiveTrueOrderByCreatedDateDesc(patient.getId());

            if (latestBody.isPresent()) {
                BodyMeasurements bm = latestBody.get();
                bodyMeasurementsDto = new NurseSummaryBodyMeasurementsDTO(
                        bm.getWeight(),
                        bm.getHeight(),
                        bm.getHeadCircumference()
                );
            }
        }
        if (bodyMeasurementsDto == null) return null;
        if (bodyMeasurementsDto.weight() == null ||
                bodyMeasurementsDto.height() == null) {
            return null;
        }

        BigDecimal height =
                bodyMeasurementsDto.height()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return bodyMeasurementsDto.weight()
                .divide(height.multiply(height), 2, RoundingMode.HALF_UP);
    }

    private List<String> getComorbidities(Long patientId) {

        return patientDiagnosisRepository.findByPatientId(patientId)
                .stream()
                .map(d -> d.getDiagnosis().getIcdShortDescription())
                .distinct()
                .toList();
    }

    private List<String> getAllergies(Long patientId) {

        return allergyRepository.findAllByPatientId(patientId)
                .stream()
                .map(a -> a.getAllergen().getName())
                .distinct()
                .toList();
    }

    private String resolveAdmissionReason(Patient patient) {

        // TODO when inpatient module ready
        return null;
    }
}