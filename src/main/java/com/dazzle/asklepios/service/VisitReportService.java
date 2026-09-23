package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.EncounterAssessment;
import com.dazzle.asklepios.domain.EncounterPlan;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientPrescription;
import com.dazzle.asklepios.domain.PatientPrescriptionMedication;
import com.dazzle.asklepios.domain.PatientProcedure;
import com.dazzle.asklepios.domain.PatientWarnings;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.domain.enumeration.AllergenTypes;
import com.dazzle.asklepios.domain.enumeration.DiagnosticOrderTestStatus;
import com.dazzle.asklepios.domain.enumeration.PrescriptionStatus;
import com.dazzle.asklepios.repository.BodyMeasurementsRepository;
import com.dazzle.asklepios.repository.DailyPatientEncounterCountProjection;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.EncounterAssessmentRepository;
import com.dazzle.asklepios.repository.EncounterPlanRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientPrescriptionRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.PatientWarningRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.repository.PrescriptionInstructionRepository;
import com.dazzle.asklepios.repository.PrescriptionMedicationRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.service.dto.PatientEncounterReportDTO;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionMedicationDTO;
import com.dazzle.asklepios.service.dto.reports.DailyPatientCountDTO;
import com.dazzle.asklepios.service.dto.reports.DailyPatientEncounterCountDTO;
import com.dazzle.asklepios.service.dto.reports.DepartmentEncounterCountDTO;
import com.dazzle.asklepios.service.dto.reports.FinancialReportDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryAllergyDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryBodyMeasurementsDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryWarningDTO;
import com.dazzle.asklepios.service.dto.reports.OrderedDiagnosticsDTO;
import com.dazzle.asklepios.service.dto.reports.ProceduresDTO;
import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
import com.dazzle.asklepios.service.dto.reports.dailyPatientVisit.DailyPatientVisitDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.report.totalDailyFootfall.TotalDailyFootfallResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    private final PractitionersRepository practitionersRepository;

    private final EncounterAssessmentRepository encounterAssessmentRepository;


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
            LOG.warn("Nurse summary is null for encounterId={}", encounterId);
            nurseSummary = new NurseSummaryReportDTO(
                    null, null, null, null, null, null,
                    null, null, null, null, null, null
            );
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
                        resolveAllergyName(a),
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

        String assessment = encounterAssessmentRepository.findFirstByEncounterIdOrderByCreatedDateDesc(encounterId)
                .map(EncounterAssessment::getAssessment)
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
                assessment,
                null,
                Instant.now()
        );
    }

    public List<FinancialReportDTO> getFinancialReport(String type, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        if (type != null) {
            type = type.trim().toUpperCase();

            if (!type.equals("SELF_PAY") && !type.equals("INSURANCE")) {
                throw new BadRequestAlertException(
                        "Invalid financial report type. Allowed values: SELF_PAY, INSURANCE", "FinancialReport", "invalid_type"
                );
            }
        }

        return patientEncounterRepository.findFinancialReport(type,startDate,endDate);
    }

    private ProceduresDTO mapProcedure(PatientProcedure entity) {
        return Optional.ofNullable(entity.getProcedure())
                .map(procedure -> new ProceduresDTO(
                        procedure.getName(),
                        procedure.getCode(),
                        lovLookupService.findDisplayValue(procedure.getCategoryType()),
                        entity.getNotes()
                ))
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
                .findAllByPrescriptionHeaderIdInAndStatusNotOrderByIdAsc(prescriptionIds, PrescriptionStatus.CANCELLED)
                .stream()
                .map(m -> new PrescriptionMedicationDTO(
                        m.getActiveIngredient().getName(),
                        m.getMedications() != null ? m.getMedications().getName() : null,
                        resolveInstruction(m),
                        m.getDuration(),
                        m.getDurationType(),
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
                .findByOrderIdInAndStatusNotOrderByIdAsc(orderIds, DiagnosticOrderTestStatus.CANCELLED)
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

    private String resolveAllergyName(PatientAllergies allergy) {

        if (allergy == null) {
            return null;
        }

        if (allergy.getAllergenType() == AllergenTypes.MEDICATION) {
            return allergy.getMedicationClass() != null
                    ? allergy.getMedicationClass().getName()
                    : null;
        }

        return allergy.getAllergen() != null
                ? allergy.getAllergen().getName()
                : null;
    }


    public List<PatientEncounterReportDTO> getAllEncounter() {

        return patientEncounterRepository
                .findAll()
                .stream()
                .map(encounter ->
                        new PatientEncounterReportDTO(

                                String.valueOf(encounter.getEncounterReason()),
                                String.valueOf(encounter.getStatus())
                        )
                )
                .toList();
    }


    public List<DailyPatientVisitDTO> getDailyPatientVisits(LocalDate visitDate) {

        LocalDate nextDate = visitDate.plusDays(1);

        return patientEncounterRepository
                .findDailyPatientVisits(visitDate, nextDate)
                .stream()
                .map(this::toDailyPatientVisitDTO)
                .toList();
    }

    public List<DailyPatientCountDTO> getDailyPatientCount(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new BadRequestAlertException(
                    "Start date and end date are required",
                    "analytics",
                    "datesrequired"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestAlertException(
                    "End date must be greater than or equal to start date",
                    "analytics",
                    "invaliddaterange"
            );
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Object[]> results =
                patientEncounterRepository.countDistinctPatientsByDay(
                        startDateTime,
                        endDateTime
                );

        Map<LocalDate, Long> countByDate = results.stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        List<DailyPatientCountDTO> response = new ArrayList<>();

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            response.add(
                    new DailyPatientCountDTO(
                            currentDate,
                            countByDate.getOrDefault(currentDate, 0L)
                    )
            );

            currentDate = currentDate.plusDays(1);
        }

        return response;
    }

    private DailyPatientVisitDTO toDailyPatientVisitDTO(PatientEncounter encounter) {

        Patient patient = encounter.getPatient();
        Practitioner practitioner = encounter.getPractitioner();
        Department department = encounter.getDepartment();

        return new DailyPatientVisitDTO(
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMedicalRecordNumber(),
                encounter.getEncounterNumber(),
                encounter.getCreatedDate(),
                department != null ? department.getName() : null,
                practitioner != null ? practitioner.getFirstName() : null,
                practitioner != null ? practitioner.getLastName() : null,
                patient.getDateOfBirth(),
               patient.getSexAtBirth()!=null? patient.getSexAtBirth().name():null,
                encounter.getStatus(),
                encounter.getEncounterStatus()
        );
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {

        if (startDate == null) {

            throw new BadRequestAlertException(
                    "startDate is required", "visitReport", "startDate.required"
            );
        }

        if (endDate == null) {

            throw new BadRequestAlertException(
                    "endDate is required", "visitReport", "endDate.required"
            );
        }

        if (endDate.isBefore(startDate)) {

            throw new BadRequestAlertException(
                    "endDate must be greater than or equal to startDate", "visitReport", "endDate.before.startDate"
            );
        }
    }


    public List<DepartmentEncounterCountDTO> getDepartmentEncounterCount(
            LocalDate startDate,
            LocalDate endDate
    ) {
       validateDates(startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return patientEncounterRepository
                .countEncountersByDepartment(
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(result -> new DepartmentEncounterCountDTO(
                        result.getDepartmentName(),
                        result.getEncounterCount(),
                        result.getPercentage()
                ))
                .toList();
    }

    public List<DailyPatientEncounterCountDTO> getDailyPatientEncounterCount(
            LocalDate startDate,
            LocalDate endDate
    ) {
       validateDates(startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<DailyPatientEncounterCountProjection> results =
                patientEncounterRepository.countPatientsAndEncountersByDay(
                        startDateTime,
                        endDateTime
                );

        Map<LocalDate, DailyPatientEncounterCountProjection> resultsByDate =
                results.stream()
                        .collect(Collectors.toMap(
                                DailyPatientEncounterCountProjection::getDate,
                                Function.identity()
                        ));

        List<DailyPatientEncounterCountDTO> response = new ArrayList<>();

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {

            DailyPatientEncounterCountProjection result =
                    resultsByDate.get(currentDate);

            response.add(
                    new DailyPatientEncounterCountDTO(
                            currentDate,
                            result != null ? result.getPatientCount() : 0L,
                            result != null ? result.getEncounterCount() : 0L
                    )
            );

            currentDate = currentDate.plusDays(1);
        }

        return response;
    }


}