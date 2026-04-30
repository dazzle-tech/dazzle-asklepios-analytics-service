package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.PatientPrescription;
import com.dazzle.asklepios.domain.PatientPrescriptionMedication;
import com.dazzle.asklepios.domain.PatientProcedure;
import com.dazzle.asklepios.domain.PatientWarnings;
import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.PatientWarningStatus;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientPrescriptionRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.PrescriptionInstructionRepository;
import com.dazzle.asklepios.repository.PrescriptionMedicationRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionMedicationDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryServiceProductDTO;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitReportService {
    private static final Logger LOG = LoggerFactory.getLogger(VisitReportService.class);

    private final NurseSummaryReportService nurseSummaryReportService;
    private final PatientProcedureRepository patientProcedureRepository;
    private final ProcedureRepository procedureRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final PrescriptionMedicationRepository prescriptionMedicationRepository;
    private final PrescriptionInstructionRepository prescriptionInstructionRepository;
    private final PatientPrescriptionRepository patientPrescriptionRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    public VisitReportDTO getVisitReport(Long encounterId) {

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
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
                        encounterId,
                        "CANCELLED"
                )
                .stream()
                .map(this::mapProcedure)
                .toList();


        return new VisitReportDTO(
                nurseSummary.patientInfo(),
                nurseSummary.encounterInfo(),
                nurseSummary.observation(),
                nurseSummary.vitalSigns(),
                nurseSummary.bodyMeasurements(),
                nurseSummary.additionalMeasurements(),
                nurseSummary.allergies(),
                nurseSummary.warnings(),
                diagnostics,
                medicationDTOS,
                procedures,
                null,
                Instant.now()
        );
    }


    private ProceduresDTO mapProcedure(PatientProcedure entity) {
        return new ProceduresDTO(
                procedureRepository.findById(entity.getProcedureId())
                        .map(Procedure::getName)
                        .orElse(null),
                procedureRepository.findById(entity.getProcedureId())
                        .map(Procedure::getCode)
                        .orElse(null),
                procedureRepository.findById(entity.getProcedureId())
                        .map(procedure ->
                                apLovValueRepository.findById(procedure.getCategoryType())
                                        .map(ApLovValue::getLovDisplayVale)
                                        .orElse(null)
                        )
                        .orElse(null),
                entity.getNotes());
    }

    private String resolveInstruction(PatientPrescriptionMedication medication) {
        if (medication == null || medication.getInstructionsType() == null) {
            LOG.debug("[resolveInstruction] medication is null or instructionsType is null");
            return null;
        }

        LOG.debug("[resolveInstruction] START medicationId={} instructionsType={} rawInstructions={}",
                medication.getId(),
                medication.getInstructionsType(),
                medication.getInstructions());

        return switch (medication.getInstructionsType()) {

            case MANUAL_INSTRUCTIONS -> {
                LOG.debug("[resolveInstruction] MANUAL_INSTRUCTIONS medicationId={} result={}",
                        medication.getId(),
                        medication.getInstructions());
                yield medication.getInstructions();
            }

            case CUSTOM_INSTRUCTIONS -> {
                LOG.debug("[resolveInstruction] CUSTOM_INSTRUCTIONS medicationId={}", medication.getId());
                String result = buildCustomInstruction(medication);
                LOG.debug("[resolveInstruction] CUSTOM_INSTRUCTIONS medicationId={} result={}",
                        medication.getId(),
                        result);
                yield result;
            }

            case PRE_DEFINED_INSTRUCTIONS -> {
                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} rawInstructions={}",
                        medication.getId(),
                        medication.getInstructions());

                Long id = safeParse(medication.getInstructions());

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} parsedId={}",
                        medication.getId(),
                        id);

                if (id == null) {
                    LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} parsedId is null",
                            medication.getId());
                    yield null;
                }

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} calling repository.findById({})",
                        medication.getId(),
                        id);

                Optional<PrescriptionInstruction> instructionOptional = prescriptionInstructionRepository.findById(id);

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} repository result present={}",
                        medication.getId(),
                        instructionOptional.isPresent());

                if (instructionOptional.isEmpty()) {
                    yield null;
                }

                PrescriptionInstruction instruction = instructionOptional.get();

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} dbInstruction dose={} unit={} rout={} frequency={}",
                        medication.getId(),
                        instruction.getDose(),
                        instruction.getUnit(),
                        instruction.getRout(),
                        instruction.getFrequency());

                String result = buildPredefinedInstruction(instruction);

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} result={}",
                        medication.getId(),
                        result);

                yield result;
            }
        };
    }

    private String buildCustomInstruction(PatientPrescriptionMedication medication) {
        LOG.debug("[buildCustomInstruction] START medicationId={}", medication.getId());

        List<String> parts = new ArrayList<>();

        if (medication.getDose() != null) {
            LOG.debug("[buildCustomInstruction] medicationId={} dose={}", medication.getId(), medication.getDose());

            String dosePart = medication.getDose().toString();

            if (medication.getDoesUnit() != null && !medication.getDoesUnit().isBlank()) {
                LOG.debug("[buildCustomInstruction] medicationId={} doseUnitKey={}",
                        medication.getId(),
                        medication.getDoesUnit());

                String unitDisplay = apLovValueRepository.findById(String.valueOf(medication.getDoesUnit()))
                        .map(ApLovValue::getLovDisplayVale)
                        .orElse(null);

                LOG.debug("[buildCustomInstruction] medicationId={} doseUnitDisplay={}",
                        medication.getId(),
                        unitDisplay);

                dosePart += " " + unitDisplay;
            }

            parts.add(dosePart);
        }

        if (medication.getRout() != null && !medication.getRout().isBlank()) {
            LOG.debug("[buildCustomInstruction] medicationId={} rout={}",
                    medication.getId(),
                    medication.getRout());
            parts.add(medication.getRout());
        }

        if (medication.getFrequency() != null && !medication.getFrequency().isBlank()) {
            LOG.debug("[buildCustomInstruction] medicationId={} frequencyKey={}",
                    medication.getId(),
                    medication.getFrequency());

            String frequencyDisplay = apLovValueRepository.findById(String.valueOf(medication.getFrequency()))
                    .map(ApLovValue::getLovDisplayVale)
                    .orElse(null);

            LOG.debug("[buildCustomInstruction] medicationId={} frequencyDisplay={}",
                    medication.getId(),
                    frequencyDisplay);

            parts.add(frequencyDisplay);
        }

        String result = parts.isEmpty() ? null : String.join(" - ", parts);

        LOG.debug("[buildCustomInstruction] END medicationId={} result={}",
                medication.getId(),
                result);

        return result;
    }

    private String buildPredefinedInstruction(PrescriptionInstruction instruction) {
        LOG.debug("[buildPredefinedInstruction] START instructionId={}",
                instruction != null ? instruction.getId() : null);

        if (instruction == null) {
            LOG.debug("[buildPredefinedInstruction] instruction is null");
            return null;
        }

        LOG.debug("[buildPredefinedInstruction] instructionId={} dose={} unit={} rout={} frequency={}",
                instruction.getId(),
                instruction.getDose(),
                instruction.getUnit(),
                instruction.getRout(),
                instruction.getFrequency());

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

        String result = sb.isEmpty() ? null : sb.toString();

        LOG.debug("[buildPredefinedInstruction] END instructionId={} result={}",
                instruction.getId(),
                result);

        return result;
    }
    private String resolveLovDisplayValues(String commaSeparatedKeys) {
        if (commaSeparatedKeys == null || commaSeparatedKeys.isBlank()) {
            return null;
        }

        List<String> keys = Arrays.stream(commaSeparatedKeys.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (keys.isEmpty()) {
            return null;
        }

        Map<String, String> lovMap = apLovValueRepository.findByKeyIn(keys).stream()
                .collect(Collectors.toMap(
                        ApLovValue::getKey,
                        ApLovValue::getLovDisplayVale
                ));

        return Arrays.stream(commaSeparatedKeys.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(lovMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }
    private List<PrescriptionMedicationDTO> getMedicationsByEncounterId(Long encounterId) {

        List<PatientPrescription> prescriptions =
                Optional.ofNullable(
                        patientPrescriptionRepository
                                .findByEncounterIdOrderByCreatedDateAsc(encounterId)
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
                        resolveLovDisplayValues(m.getAdministrationInstructions()),
                        m.getAllowedSubstitute(),
                        m.getIndicationIcd() != null
                                ? m.getIndicationIcd().getIcdShortDescription()
                                : null
                ))
                .toList();
    }

    private Long safeParse(String value) {
        LOG.debug("[safeParse] rawValue={}", value);
        try {
            Long parsed = value != null ? Long.parseLong(value) : null;
            LOG.debug("[safeParse] parsedValue={}", parsed);
            return parsed;
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

        // 🔥 map orderId → order
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
    }}