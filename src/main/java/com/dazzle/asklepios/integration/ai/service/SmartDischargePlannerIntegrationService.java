package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientDiagnosis;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientProblem;
import com.dazzle.asklepios.domain.enumeration.DiagnosisType;
import com.dazzle.asklepios.domain.enumeration.DiagnosticStatus;
import com.dazzle.asklepios.domain.enumeration.PatientHistoryStatus;
import com.dazzle.asklepios.integration.ai.client.SmartDischargePlannerClient;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.ClinicalDataDTO;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.ClinicalNoteDTO;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.DischargePlanningRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.DischargePlanningResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.OperationalDataDTO;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.PatientContextDTO;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.PendingTestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.VitalReadingDTO;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientProblemRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SmartDischargePlannerIntegrationService {

    private static final Logger LOG = LoggerFactory.getLogger(SmartDischargePlannerIntegrationService.class);

    // a test is "pending" while it's anywhere in the workflow before RESULT_READY -
    // once a result exists (ready/approved/completed/rejected) it's no longer awaited
    private static final Set<DiagnosticStatus> PENDING_TEST_STATUSES = Set.of(
            DiagnosticStatus.NEW,
            DiagnosticStatus.SUBMITTED,
            DiagnosticStatus.PATIENT_ARRIVED,
            DiagnosticStatus.ACCEPTED,
            DiagnosticStatus.SAMPLE_COLLECTED,
            DiagnosticStatus.SAMPLE_REPEAT
    );

    private final SmartDischargePlannerClient smartDischargePlannerClient;
    private final PatientEncounterRepository patientEncounterRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final PatientProblemRepository patientProblemRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final PatientAiContextBuilderService patientAiContextBuilderService;

    public DischargePlanningResponseDTO assessDischargeReadiness(Long encounterId) {
        LOG.debug("[SMART_DISCHARGE_PLANNER] assessing discharge readiness for encounterId={}", encounterId);

        PatientEncounter encounter = patientEncounterRepository.findById(encounterId)
                .orElseThrow(() -> new NotFoundAlertException("Encounter not found with id " + encounterId, "encounter", "notfound"));
        Patient patient = encounter.getPatient();

        DischargePlanningRequestDTO request = new DischargePlanningRequestDTO(
                UUID.randomUUID().toString(),
                buildPatientContext(patient, encounter),
                buildClinicalData(patient, encounter),
                buildOperationalData()
        );

        return smartDischargePlannerClient.planDischarge(request);
    }

    private PatientContextDTO buildPatientContext(Patient patient, PatientEncounter encounter) {
        return new PatientContextDTO(
                String.valueOf(patient.getId()),
                encounter.getEncounterDate() != null ? encounter.getEncounterDate().toString() : null,
                patientAiContextBuilderService.buildDiagnosis(encounter.getId()),
                buildSecondaryDiagnoses(encounter.getId()),
                encounter.getStatus()
        );
    }

    private List<String> buildSecondaryDiagnoses(Long encounterId) {
        return patientDiagnosisRepository.findByEncounterIdOrderByCreatedDateAsc(encounterId)
                .stream()
                .filter(d -> d.getType() != DiagnosisType.PRIMARY && d.getDiagnosis() != null)
                .map(d -> d.getDiagnosis().getIcdShortDescription())
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private ClinicalDataDTO buildClinicalData(Patient patient, PatientEncounter encounter) {
        return new ClinicalDataDTO(
                buildLatestVitals(encounter.getId()),
                buildPendingTests(encounter.getId()),
                buildActiveProblems(patient.getId()),
                patientAiContextBuilderService.buildCurrentMedications(patient.getId()),
                List.of(), // no discharge-specific medication order source exists yet
                buildClinicalNotes(encounter.getId())
        );
    }

    private List<VitalReadingDTO> buildLatestVitals(Long encounterId) {
        return patientAiContextBuilderService.buildVitals(encounterId)
                .entrySet()
                .stream()
                .map(entry -> new VitalReadingDTO(entry.getKey(), entry.getValue(), null, null))
                .toList();
    }

    private List<PendingTestDTO> buildPendingTests(Long encounterId) {
        List<Long> orderIds = diagnosticOrderRepository.findByEncounterIdOrderByCreatedDateAsc(encounterId)
                .stream()
                .map(DiagnosticOrder::getId)
                .toList();

        if (orderIds.isEmpty()) {
            return List.of();
        }

        List<DiagnosticOrderTest> pendingOrderTests = diagnosticOrderTestRepository.findByOrderIdIn(orderIds)
                .stream()
                .filter(test -> test.getProcessingStatus() == null || PENDING_TEST_STATUSES.contains(test.getProcessingStatus()))
                .toList();

        List<Long> testIds = pendingOrderTests.stream()
                .map(DiagnosticOrderTest::getTestId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        java.util.Map<Long, DiagnosticTest> testById = testIds.isEmpty()
                ? java.util.Map.of()
                : diagnosticTestRepository.findAllById(testIds).stream()
                        .collect(java.util.stream.Collectors.toMap(DiagnosticTest::getId, t -> t, (a, b) -> a));

        return pendingOrderTests.stream()
                .map(test -> toPendingTestDTO(test, testById))
                .toList();
    }

    private PendingTestDTO toPendingTestDTO(DiagnosticOrderTest test, java.util.Map<Long, DiagnosticTest> testById) {
        DiagnosticTest diagnosticTest = test.getTestId() != null ? testById.get(test.getTestId()) : null;
        String name = diagnosticTest != null ? diagnosticTest.getName() : "Unknown test";
        String status = test.getProcessingStatus() != null ? test.getProcessingStatus().name() : "pending";

        return new PendingTestDTO(name, status);
    }

    private List<String> buildActiveProblems(Long patientId) {
        return patientProblemRepository.findByPatientIdAndStatus(patientId, PatientHistoryStatus.ACTIVE)
                .stream()
                .map(PatientProblem::getCondition)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private List<ClinicalNoteDTO> buildClinicalNotes(Long encounterId) {
        String notes = patientAiContextBuilderService.buildClinicalNotes(encounterId);

        if (notes == null || notes.isBlank()) {
            return List.of();
        }

        return List.of(new ClinicalNoteDTO("progress_note", null, notes));
    }

    private OperationalDataDTO buildOperationalData() {
        // no scheduling/education/transport/home-support/equipment data sources exist in the
        // domain model yet - send empty defaults rather than guessing at nonexistent entities.
        return new OperationalDataDTO(List.of(), List.of(), null, null, List.of());
    }
}
