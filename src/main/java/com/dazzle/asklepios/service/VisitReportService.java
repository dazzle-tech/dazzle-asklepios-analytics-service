package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.EncounterPlan;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientPrescription;
import com.dazzle.asklepios.domain.PatientPrescriptionMedication;
import com.dazzle.asklepios.domain.PatientProcedure;
import com.dazzle.asklepios.domain.PatientWarnings;
import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.repository.BodyMeasurementsRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.EncounterPlanRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientPrescriptionRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.PatientWarningRepository;
import com.dazzle.asklepios.repository.PrescriptionInstructionRepository;
import com.dazzle.asklepios.repository.PrescriptionMedicationRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionMedicationDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryAllergyDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryBodyMeasurementsDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryWarningDTO;
import com.dazzle.asklepios.service.dto.reports.OrderedDiagnosticsDTO;
import com.dazzle.asklepios.service.dto.reports.ProceduresDTO;
import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitReportService {

    private static final Logger LOG = LoggerFactory.getLogger(VisitReportService.class);

    private final NurseSummaryReportService nurseSummaryReportService;
    private final LovLookupService lovLookupService;
    private final ReportCommonService reportCommonService;

    private final PatientProcedureRepository patientProcedureRepository;
    private final ProcedureRepository procedureRepository;
    private final PrescriptionMedicationRepository prescriptionMedicationRepository;
    private final PrescriptionInstructionRepository prescriptionInstructionRepository;
    private final PatientPrescriptionRepository patientPrescriptionRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final PatientAllergyRepository patientAllergyRepository;
    private final PatientWarningRepository patientWarningRepository;
    private final BodyMeasurementsRepository bodyMeasurementsRepository;
    private final PatientEncounterRepository patientEncounterRepository;
    private final EncounterPlanRepository encounterPlanRepository;

    public VisitReportDTO getVisitReport(Long encounterId) {
        PatientEncounter encounter = patientEncounterRepository.findById(encounterId).orElse(null);
        if (encounter == null) {
            LOG.debug("[getVisitReport] No encounter found for id={}", encounterId);
            return null;
        }

        Patient patient = encounter.getPatient();

        NurseSummaryReportDTO nurseSummary =
                nurseSummaryReportService.getNurseSummaryReport(encounterId);

        if (nurseSummary == null) {
            return null;
        }

        List<OrderedDiagnosticsDTO> diagnostics =
                getDiagnosticsByEncounterId(encounterId);

        List<PrescriptionMedicationDTO> medicationDTOS =
                getMedicationsByEncounterId(encounterId);

        List<ProceduresDTO> procedures = patientProcedureRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(encounterId, "CANCELLED")
                .stream()
                .map(this::mapProcedure)
                .toList();

        List<PatientAllergies> allergies =
                Optional.ofNullable(patientAllergyRepository.findAllByPatientId(patient.getId()))
                        .orElse(Collections.emptyList());

        List<PatientWarnings> warnings =
                Optional.ofNullable(patientWarningRepository.findAllByPatientId(patient.getId()))
                        .orElse(Collections.emptyList());

        List<NurseSummaryAllergyDTO> allergyDTOS = allergies.stream()
                .map(a -> new NurseSummaryAllergyDTO(
                        a.getAllergenType() != null ? a.getAllergenType() : null,
                        a.getAllergen() != null ? a.getAllergen().getName() : null,
                        a.getSeverity().name()
                ))
                .toList();

        List<NurseSummaryWarningDTO> warningDTOS = warnings.stream()
                .map(w -> new NurseSummaryWarningDTO(
                        w.getWarningType() != null
                                ? reportCommonService.getLovDisplayValue(String.valueOf(w.getWarningType()))
                                : null,
                        w.getWarning(),
                        w.getSeverity() != null ? w.getSeverity().name() : null,
                        w.getOnsetDate(),
                        w.isByPatient(),
                        w.getSourceOfInformation(),
                        w.getNote(),
                        w.getActionTaken(),
                        w.getStatus() != null ? w.getStatus().name() : null
                ))
                .toList();

        NurseSummaryBodyMeasurementsDTO bodyMeasurementsDto = null;
        if (patient != null) {
            Optional<com.dazzle.asklepios.domain.BodyMeasurements> latestBody =
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

        String plan = encounterPlanRepository.findTopByEncounterIdOrderByCreatedDateDesc(encounterId)
                .map(EncounterPlan::getTreatmentPlan)
                .orElse(null);

        return new VisitReportDTO(
                nurseSummary.patientInfo(),
                nurseSummary.encounterInfo(),
                nurseSummary.observation(),
                nurseSummary.vitalSigns(),
                bodyMeasurementsDto != null ? bodyMeasurementsDto : nurseSummary.bodyMeasurements(),
                nurseSummary.additionalMeasurements(),
                allergyDTOS,
                warningDTOS,
                diagnostics,
                medicationDTOS,
                procedures,
                plan,
                null,
                Instant.now()
        );
    }

    private ProceduresDTO mapProcedure(PatientProcedure entity) {
        return procedureRepository.findById(entity.getProcedureId())
                .map(procedure -> {
                    String categoryDisplay = lovLookupService.findDisplayValue(procedure.getCategoryType());
                    return new ProceduresDTO(
                            procedure.getName(),
                            procedure.getCode(),
                            categoryDisplay,
                            entity.getNotes()
                    );
                })
                .orElse(new ProceduresDTO(null, null, null, entity.getNotes()));
    }

    private String resolveInstruction(PatientPrescriptionMedication medication) {
        if (medication == null || medication.getInstructionsType() == null) {
            LOG.debug("[resolveInstruction] medication is null or instructionsType is null");
            return null;
        }

        return switch (medication.getInstructionsType()) {
            case MANUAL_INSTRUCTIONS -> medication.getInstructions();

            case CUSTOM_INSTRUCTIONS -> buildCustomInstruction(medication);

            case PRE_DEFINED_INSTRUCTIONS -> {
                Long id = safeParse(medication.getInstructions());
                if (id == null) {
                    yield null;
                }

                Optional<PrescriptionInstruction> instructionOptional =
                        prescriptionInstructionRepository.findById(id);

                if (instructionOptional.isEmpty()) {
                    yield null;
                }

                yield buildPredefinedInstruction(instructionOptional.get());
            }
        };
    }

    private String buildCustomInstruction(PatientPrescriptionMedication medication) {
        List<String> parts = new ArrayList<>();

        if (medication.getDose() != null) {
            String dosePart = medication.getDose().toString();

            if (medication.getDoesUnit() != null && !medication.getDoesUnit().isBlank()) {
                String unitDisplay =
                        lovLookupService.findDisplayValue(String.valueOf(medication.getDoesUnit()));
                dosePart += " " + unitDisplay;
            }

            parts.add(dosePart);
        }

        if (medication.getRout() != null && !medication.getRout().isBlank()) {
            parts.add(medication.getRout());
        }

        if (medication.getFrequency() != null && !medication.getFrequency().isBlank()) {
            String frequencyDisplay =
                    lovLookupService.findDisplayValue(String.valueOf(medication.getFrequency()));
            parts.add(frequencyDisplay);
        }

        return parts.isEmpty() ? null : String.join(" - ", parts);
    }

    private String buildPredefinedInstruction(PrescriptionInstruction instruction) {
        if (instruction == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (instruction.getDose() != null) {
            sb.append(instruction.getDose());
        }

        if (instruction.getUnit() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(instruction.getUnit());
        }

        if (instruction.getRout() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(instruction.getRout());
        }

        if (instruction.getFrequency() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(instruction.getFrequency());
        }

        return sb.isEmpty() ? null : sb.toString();
    }

    private List<PrescriptionMedicationDTO> getMedicationsByEncounterId(Long encounterId) {
        List<PatientPrescription> prescriptions =
                Optional.ofNullable(
                        patientPrescriptionRepository.findByEncounterIdOrderByCreatedDateAsc(encounterId)
                ).orElse(List.of());

        if (prescriptions.isEmpty()) {
            return List.of();
        }

        List<Long> prescriptionIds = prescriptions.stream()
                .map(PatientPrescription::getId)
                .toList();

        return prescriptionMedicationRepository
                .findAllByPrescriptionHeaderIdInOrderByIdAsc(prescriptionIds)
                .stream()
                .map(m -> new PrescriptionMedicationDTO(
                        m.getMedications() != null ? m.getMedications().getName() : null,
                        resolveInstruction(m),
                        m.getDuration(),
                        m.getNumberOfRefills() != null && m.getNumberOfRefills() > 0,
                        m.getNumberOfRefills(),
                        reportCommonService.getLovDisplayValues(m.getAdministrationInstructions()),
                        m.getAllowedSubstitute(),
                        m.getIndicationIcd() != null
                                ? m.getIndicationIcd().getIcdShortDescription()
                                : null
                ))
                .toList();
    }

    private Long safeParse(String value) {
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            LOG.debug("[safeParse] failed to parse value={}", value, e);
            return null;
        }
    }

    private List<OrderedDiagnosticsDTO> getDiagnosticsByEncounterId(Long encounterId) {
        List<DiagnosticOrder> orders =
                Optional.ofNullable(
                        diagnosticOrderRepository.findByEncounterIdOrderByCreatedDateAsc(encounterId)
                ).orElse(List.of());

        if (orders.isEmpty()) {
            return List.of();
        }

        Map<Long, DiagnosticOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(DiagnosticOrder::getId, o -> o));

        List<Long> orderIds = orders.stream()
                .map(DiagnosticOrder::getId)
                .toList();

        return diagnosticOrderTestRepository
                .findByOrderIdInOrderByIdAsc(orderIds)
                .stream()
                .map(test -> {
                    DiagnosticOrder order = orderMap.get(test.getOrderId());

                    DiagnosticTest diagnosticTest = diagnosticTestRepository
                            .findById(test.getTestId())
                            .orElse(null);

                    return new OrderedDiagnosticsDTO(
                            order != null ? order.getOrderNumber() : null,
                            diagnosticTest != null ? diagnosticTest.getName() : null,
                            diagnosticTest != null ? diagnosticTest.getType() : null
                    );
                })
                .toList();
    }
}