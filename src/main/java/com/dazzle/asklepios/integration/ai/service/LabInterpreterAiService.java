package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.enumeration.DiagnosticStatus;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.integration.ai.client.LabInterpreterClient;
import com.dazzle.asklepios.integration.ai.client.dto.lab.ConditionDTO;
import com.dazzle.asklepios.integration.ai.client.dto.lab.LabInterpretationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.lab.LabInterpretationResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.lab.LabResultDTO;
import com.dazzle.asklepios.integration.ai.client.dto.lab.MedicationDTO;
import com.dazzle.asklepios.integration.ai.client.dto.lab.PatientContextDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.LabInterpretationRequestVM;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.CurrentMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestResultRepository;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.ReportCommonService;
import com.dazzle.asklepios.web.rest.VisitReportController;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabInterpreterAiService {
    private static final Logger LOG = LoggerFactory.getLogger(LabInterpreterAiService.class);

    private final PatientRepository patientRepository;
    private final DiagnosticOrderRepository orderRepository;
    private final DiagnosticOrderTestRepository orderTestRepository;
    private final DiagnosticOrderTestResultRepository resultRepository;
    private final DiagnosticTestProfileRepository profileRepository;
    private final CurrentMedicationRepository medicationRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;

    private final ReportCommonService reportCommonService;
    private final LabInterpreterClient client;


    public LabInterpretationResponseDTO interpret(LabInterpretationRequestVM request) {

        LabInterpretationRequestDTO aiRequest = buildDebugRequest(request);

        LOG.debug("Lab interpretation request built");
        LOG.debug("Request ID: {}", aiRequest.request_id());

        LOG.debug("Lab results count: {}",
                aiRequest.lab_results() != null ? aiRequest.lab_results().size() : 0);

        LOG.debug("Medications count: {}",
                aiRequest.medications() != null ? aiRequest.medications().size() : 0);

        LOG.debug("Patient context: {}", aiRequest.patient_context());

        return client.interpret(aiRequest);
    }


    public LabInterpretationRequestDTO buildDebugRequest(LabInterpretationRequestVM request) {

        validateRequest(request);

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found", "patient", "notfound"
                ));

        List<Long> orderTestIds = getOrderTestIds(request.patientId());

        LOG.debug("Patient ID={}", request.patientId());
        LOG.debug("Date From={}", request.dateFrom());
        LOG.debug("Date To={}", request.dateTo());
        LOG.debug("Order Test IDs Count={}", orderTestIds.size());
        LOG.debug("Order Test IDs={}", orderTestIds);

        List<DiagnosticOrderTestResult> currentResults = orderTestIds.isEmpty()
                ? List.of()
                : resultRepository.findByOrderTestIdInAndApprovedDateBetweenAndProcessingStatus(
                orderTestIds,
                request.dateFrom(),
                request.dateTo(),
                DiagnosticStatus.RESULT_APPROVED
        );

        LOG.debug("Current Results Count={}", currentResults.size());

        List<LabResultDTO> labResults = mapLabResults(currentResults);

        LOG.debug("Mapped Lab Results Count={}", labResults.size());

        if (!labResults.isEmpty()) {
            LOG.debug("First Lab Result={}", labResults.get(0));
        }

        List<MedicationDTO> medications = mapMedications(request.patientId());

        LOG.debug("Medications Count={}", medications.size());

        PatientContextDTO patientContext = buildPatientContext(patient);

        LOG.debug("Patient Context={}", patientContext);

        return new LabInterpretationRequestDTO(
                UUID.randomUUID().toString(),
                patientContext,
                labResults,
                medications
        );
    }
    // =========================
    // ✅ VALIDATION
    // =========================

    private void validateRequest(LabInterpretationRequestVM request) {
        if (request.dateFrom() == null || request.dateTo() == null) {
            throw new BadRequestAlertException(
                    "Date range is required", "lab_interpretation", "date_required"
            );
        }

        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new BadRequestAlertException(
                    "Invalid date range", "lab_interpretation", "date_invalid"
            );
        }
    }

    // =========================
    // ✅ ORDER TEST IDS
    // =========================

    private List<Long> getOrderTestIds(Long patientId) {

        List<DiagnosticOrder> orders =
                orderRepository.findByPatientId(patientId);

        LOG.debug("Orders Count={}", orders.size());

        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream()
                .map(DiagnosticOrder::getId)
                .toList();

        LOG.debug("Order IDs={}", orderIds);

        List<DiagnosticOrderTest> orderTests =
                orderTestRepository.findByOrderIdIn(orderIds);

        LOG.debug("Order Tests Count={}", orderTests.size());

        LOG.debug("Order Test IDs={}",
                orderTests.stream()
                        .map(DiagnosticOrderTest::getId)
                        .toList());

        return orderTests.stream()
                .map(DiagnosticOrderTest::getId)
                .toList();
    }
    // =========================
    // ✅ PATIENT CONTEXT 👑
    // =========================
    private PatientContextDTO buildPatientContext(Patient patient) {
        return new PatientContextDTO(
                calculateAgeInYears(patient),
                patient.getSexAtBirth() != null
                        ? patient.getSexAtBirth().toString()
                        : "Unknown",
                mapConditionsForAi(getPatientDiagnoses(patient.getId())),
                getClinicalContext(patient.getId())
        );
    }

    private List<ConditionDTO> getPatientDiagnoses(Long patientId) {
        return patientDiagnosisRepository.findByPatientId(patientId)
                .stream()
                .map(d -> new ConditionDTO(
                        d.getDiagnosis().getIcdShortDescription(),
                        d.getCreatedDate() != null
                                ? d.getCreatedDate()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                                : null
                ))
                .toList();
    }

    private String getClinicalContext(Long patientId) {

        List<DiagnosticOrder> orders = orderRepository.findByPatientId(patientId);

        if (orders.isEmpty()) {
            return null;
        }

        List<Long> orderIds = orders.stream()
                .map(DiagnosticOrder::getId)
                .toList();

        List<DiagnosticOrderTest> orderTests =
                orderTestRepository.findByOrderIdIn(orderIds);

        return orderTests.stream()
                .map(DiagnosticOrderTest::getDiagnosis)
                .filter(Objects::nonNull)
                .map(ICDDiagnosis::getIcdShortDescription)
                .filter(Objects::nonNull)
                .distinct()
                .limit(2)
                .collect(Collectors.joining(", "));
    }


    // =========================
    // ✅ MEDICATIONS
    // =========================

    private List<MedicationDTO> mapMedications(Long patientId) {
        return medicationRepository.findByPatientId(patientId)
                .stream()
                .map(m -> new MedicationDTO(
                        m.getActiveIngredient().getName(),
                        m.getStartDate(),
                        m.getCancelledDate()
                ))
                .toList();
    }

    // =========================
    // ✅ LAB MAPPING
    // =========================

    private List<LabResultDTO> mapLabResults(List<DiagnosticOrderTestResult> results) {

        if (results.isEmpty()) {
            return List.of();
        }

        List<Long> profileIds = results.stream()
                .map(DiagnosticOrderTestResult::getProfileTestId)
                .distinct()
                .toList();

        Map<Long, DiagnosticTestProfile> profileMap =
                profileRepository.findAllById(profileIds)
                        .stream()
                        .collect(Collectors.toMap(DiagnosticTestProfile::getId, p -> p));

        return results.stream()
                .map(result -> {

                    DiagnosticTestProfile profile =
                            profileMap.get(result.getProfileTestId());

                    return new LabResultDTO(
                            resolveTestName(profile),
                            resolveResultValue(result, profile),
                            resolveUnit(profile),
                            resolveReferenceRange(result, profile),
                            mapFlag(result),
                            result.getApprovedDate()
                    );
                })
                .toList();
    }

    // =========================
    // ✅ FIELD RESOLVERS
    // =========================

    private String resolveTestName(DiagnosticTestProfile profile) {
        return profile != null ? profile.getName() : "Unknown";
    }

    private String resolveUnit(DiagnosticTestProfile profile) {
        if (profile == null || profile.getResultUnit() == null) return null;

        return reportCommonService.getLovDisplayValue(
                String.valueOf(profile.getResultUnit())
        );
    }

    private String resolveResultValue(DiagnosticOrderTestResult result, DiagnosticTestProfile profile) {

        if (profile != null && profile.getResultType() == TestResultType.LOV) {
            return apLovValueRepository
                    .findById(String.valueOf(result.getResultValueText()))
                    .map(ApLovValue::getLovDisplayVale)
                    .orElse(result.getResultValueText());
        }

        return result.getResultValueNumber() != null
                ? result.getResultValueNumber().toString()
                : result.getResultValueText();
    }

    private String resolveReferenceRange(DiagnosticOrderTestResult result, DiagnosticTestProfile profile) {

        if (profile != null && profile.getResultType() == TestResultType.LOV) {
            return apLovValueRepository
                    .findById(String.valueOf(result.getNormalRangeValue()))
                    .map(ApLovValue::getLovDisplayVale)
                    .orElse(result.getNormalRangeValue());
        }

        return result.getNormalRangeValue();
    }

    private String mapFlag(DiagnosticOrderTestResult result) {
        return result.getMarker() != null
                ? result.getMarker().name().toLowerCase()
                : "not defined";
    }

    // =========================
    // ✅ AGE
    // =========================

    private Integer calculateAgeInYears(Patient patient) {

        if (patient.getDateOfBirth() == null) {
            return null;
        }

        return java.time.Period.between(
                patient.getDateOfBirth()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(),
                java.time.LocalDate.now()
        ).getYears();
    }

    private List<String> mapConditionsForAi(List<ConditionDTO> conditions) {

        if (conditions == null || conditions.isEmpty()) {
            return List.of();
        }

        return conditions.stream()
                .map(c -> c.name())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}