package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.AdditionalMeasurements;
import com.dazzle.asklepios.domain.BodyMeasurements;
import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.EncounterPlan;
import com.dazzle.asklepios.domain.EncounterVaccination;
import com.dazzle.asklepios.domain.PainAssessment;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientObservationsComplaints;
import com.dazzle.asklepios.domain.PatientProcedure;
import com.dazzle.asklepios.domain.PatientServiceAndProduct;
import com.dazzle.asklepios.domain.PatientWarnings;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.VitalSigns;
import com.dazzle.asklepios.domain.enumeration.DiagnosisType;
import com.dazzle.asklepios.domain.enumeration.DiagnosticOrderTestStatus;
import com.dazzle.asklepios.domain.enumeration.EncounterVaccinationStatus;
import com.dazzle.asklepios.domain.enumeration.PatientAllergyStatus;
import com.dazzle.asklepios.domain.enumeration.PatientWarningStatus;
import com.dazzle.asklepios.domain.enumeration.PrescriptionStatus;
import com.dazzle.asklepios.domain.enumeration.ProcStatus;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.EncounterPlanRepository;
import com.dazzle.asklepios.repository.EncounterVaccinationRepository;
import com.dazzle.asklepios.repository.PatientAllergiesRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientPrescriptionMedicationRepository;
import com.dazzle.asklepios.repository.PatientPrescriptionRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.PatientServiceAndProductRepository;
import com.dazzle.asklepios.repository.PatientWarningsRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.service.dto.reports.BrandMedicationsDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryAdditionalMeasurementsDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryAllergyDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryBodyMeasurementsDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryEncounterInfoDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryObservationDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryPatientInfoDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryServiceProductDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryVaccinationDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryVitalSignsDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryWarningDTO;
import com.dazzle.asklepios.service.dto.reports.OrderedDiagnosticsDTO;
import com.dazzle.asklepios.service.dto.reports.ProceduresDTO;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NurseSummaryReportService {

    private static final Logger LOG = LoggerFactory.getLogger(NurseSummaryReportService.class);

    private final PatientEncounterService patientEncounterService;
    private final PatientObservationsComplaintsService patientObservationsComplaintsService;
    private final VitalSignsService vitalSignsService;
    private final BodyMeasurementsService bodyMeasurementsService;
    private final AdditionalMeasurementsService additionalMeasurementsService;
    private final DepartmentsRepository departmentsRepository;
    private final PatientAllergiesRepository patientAllergiesRepository;
    private final PatientWarningsRepository patientWarningsRepository;
    private final EncounterVaccinationRepository encounterVaccinationRepository;
    private final PatientServiceAndProductRepository patientServiceAndProductRepository;
    private final PatientEncounterRepository encounterRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final PainAssessmentService painAssessmentService;
    private final EncounterPlanRepository encounterPlanRepository;
    private final PatientProcedureRepository patientProcedureRepository;
    private final ProcedureRepository procedureRepository;
    private final PatientPrescriptionRepository patientPrescriptionRepository;
    private final PatientPrescriptionMedicationRepository patientPrescriptionMedicationRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final ApLovValueRepository apLovValueRepository;

    // ── LOV resolver ──────────────────────────────────────────────────────────

    private Map<String, String> buildLovMap(List<String> keys) {
        List<String> filtered = keys.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (filtered.isEmpty()) {
            return Map.of();
        }

        return apLovValueRepository.findByKeyIn(filtered)
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getKey(),
                        v -> v.getLovDisplayVale() != null ? v.getLovDisplayVale() : v.getKey()
                ));
    }

    private String resolve(Map<String, String> lovMap, String key) {
        if (key == null) return null;
        return lovMap.getOrDefault(key, key);
    }

    // ── Main method ───────────────────────────────────────────────────────────

    public NurseSummaryReportDTO getNurseSummaryReport(Long encounterId) {
        LOG.debug("[NURSE_SUMMARY] start encounterId={}", encounterId);

        PatientEncounter encounter = patientEncounterService.getById(encounterId);
        Patient patient = encounter.getPatient();

        // ── Fetch raw data first ───────────────────────────────────────────────

        VitalSigns rawVitalSigns = vitalSignsService
                .findLatestByEncounterId(encounterId)
                .orElse(null);

        PainAssessment painAssessment = painAssessmentService
                .findLatestByEncounterId(encounterId)
                .orElse(null);

        List<PatientServiceAndProduct> rawServicesAndProducts =
                patientServiceAndProductRepository.findByEncounterIdOrderByCreatedDateAsc(encounterId);

        List<PatientWarnings> rawWarnings = patientWarningsRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
                        encounterId, PatientWarningStatus.CANCELLED);

        List<PatientAllergies> rawAllergies =
                (List<PatientAllergies>) patientAllergiesRepository
                        .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
                                encounterId, PatientAllergyStatus.CANCELLED);

        List<PatientProcedure> rawPatientProcedures =
                patientProcedureRepository
                        .findByEncounter_IdOrderByCreatedDateAsc(encounterId)
                        .stream()
                        .filter(p -> p.getStatus() != ProcStatus.CANCELLED)
                        .toList();

        // ── Collect all LOV keys in one shot ──────────────────────────────────

        List<String> lovKeys = new ArrayList<>();

        if (rawVitalSigns != null) {
            lovKeys.add(rawVitalSigns.getMeasurementSite());
        }
        if (painAssessment != null) {
            lovKeys.add(painAssessment.getPainDegree());
            lovKeys.add(painAssessment.getPainPattern());
        }
        rawServicesAndProducts.forEach(sp ->
                lovKeys.add(sp.getCategory() != null ? sp.getCategory().name() : null)
        );
        rawWarnings.forEach(w -> lovKeys.add(w.getWarningType()));
        rawAllergies.forEach(a -> {
            lovKeys.add(a.getAllergenType() != null ? a.getAllergenType().name() : null);
            lovKeys.add(a.getCriticality());
            lovKeys.add(a.getAllergicReactions());
            lovKeys.add(a.getOnset());
        });
        rawPatientProcedures.forEach(p ->
                procedureRepository.findById(p.getProcedureId())
                        .ifPresent(proc -> lovKeys.add(proc.getCategoryType()))
        );

        Map<String, String> lovMap = buildLovMap(lovKeys);


        NurseSummaryPatientInfoDTO patientInfo = mapPatient(patient);
        NurseSummaryEncounterInfoDTO encounterInfo = mapEncounter(encounter);

        String plan = encounterPlanRepository
                .findTopByEncounterIdOrderByCreatedDateDesc(encounterId)
                .map(EncounterPlan::getPlanInstructions)
                .orElse(null);

        String diagnosis = patientDiagnosisRepository
                .findWithDiagnosisByEncounterIdAndType(encounterId, DiagnosisType.PRIMARY)
                .map(d -> d.getDiagnosis() != null
                        ? d.getDiagnosis().getIcdShortDescription()
                        : null)
                .orElseGet(() ->
                        patientDiagnosisRepository
                                .findByEncounterIdOrderByCreatedDateDesc(encounterId)
                                .stream()
                                .filter(d -> d.getDiagnosis() != null)
                                .map(d -> d.getDiagnosis().getIcdShortDescription())
                                .findFirst()
                                .orElse(null)
                );

        NurseSummaryObservationDTO observation = patientObservationsComplaintsService
                .findLatestByEncounterId(encounterId)
                .map(entity -> mapObservation(entity, diagnosis, plan))
                .orElseGet(() -> (diagnosis != null || plan != null)
                        ? new NurseSummaryObservationDTO(null, null, null, null, diagnosis, plan)
                        : null
                );


        NurseSummaryVitalSignsDTO vitalSigns = null;

        if (rawVitalSigns != null) {
            vitalSigns = new NurseSummaryVitalSignsDTO(
                    rawVitalSigns.getBloodPressureSystolic(),
                    rawVitalSigns.getBloodPressureDiastolic(),
                    resolve(lovMap, rawVitalSigns.getMeasurementSite()),
                    rawVitalSigns.getHeartRate(),
                    rawVitalSigns.getTemperature(),
                    rawVitalSigns.getOxygenSaturation(),
                    rawVitalSigns.getRespiratoryRate(),
                    rawVitalSigns.getNotes(),
                    null, null, null, null
            );
        }

        if (painAssessment != null) {
            String painDegree  = resolve(lovMap, painAssessment.getPainDegree());
            String painLevel   = painAssessment.getPainLevel() != null
                    ? painAssessment.getPainLevel().name() : null;
            String painPattern = resolve(lovMap, painAssessment.getPainPattern());
            String painDesc    = painAssessment.getPainDescription();

            if (vitalSigns != null) {
                vitalSigns = new NurseSummaryVitalSignsDTO(
                        vitalSigns.bloodPressureSystolic(),
                        vitalSigns.bloodPressureDiastolic(),
                        vitalSigns.measurementSite(),
                        vitalSigns.heartRate(),
                        vitalSigns.temperature(),
                        vitalSigns.oxygenSaturation(),
                        vitalSigns.respiratoryRate(),
                        vitalSigns.notes(),
                        painDegree,
                        painLevel,
                        painPattern,
                        painDesc
                );
            } else {
                vitalSigns = new NurseSummaryVitalSignsDTO(
                        null, null, null, null, null, null, null, null,
                        painDegree, painLevel, painPattern, painDesc
                );
            }
        }


        NurseSummaryBodyMeasurementsDTO bodyMeasurements = bodyMeasurementsService
                .findLatestByEncounterId(encounterId)
                .map(this::mapBodyMeasurements)
                .orElse(null);

        NurseSummaryAdditionalMeasurementsDTO additionalMeasurements = additionalMeasurementsService
                .findLatestByEncounterId(encounterId)
                .map(this::mapAdditionalMeasurements)
                .orElse(null);

        List<NurseSummaryAllergyDTO> allergies = rawAllergies
                .stream()
                .map(a -> mapAllergy(a, lovMap))
                .toList();

        List<NurseSummaryWarningDTO> warnings = rawWarnings
                .stream()
                .map(entity -> mapWarning(entity, lovMap))
                .toList();


        List<NurseSummaryVaccinationDTO> vaccinations = encounterVaccinationRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
                        encounterId, EncounterVaccinationStatus.CANCELLED)
                .stream()
                .map(this::mapVaccination)
                .toList();

        List<NurseSummaryServiceProductDTO> servicesAndProducts = rawServicesAndProducts
                .stream()
                .map(entity -> mapServiceAndProduct(entity, lovMap))
                .toList();


        List<OrderedDiagnosticsDTO> diagnosticsOrder =
                diagnosticOrderRepository
                        .findByEncounterId(encounterId)
                        .stream()
                        .flatMap(order ->
                                diagnosticOrderTestRepository
                                        .findByOrderIdOrderByIdAsc(order.getId())
                                        .stream()
                                        .filter(test -> test.getStatus() != DiagnosticOrderTestStatus.CANCELLED)
                                        .map(test -> {
                                            DiagnosticTest diagnosticTest = diagnosticTestRepository
                                                    .findById(test.getTestId())
                                                    .orElse(null);
                                            return new OrderedDiagnosticsDTO(
                                                    order.getOrderNumber(),
                                                    diagnosticTest != null ? diagnosticTest.getName() : null,
                                                    test.getOrderType() != null ? test.getOrderType().name() : null
                                            );
                                        })
                        )
                        .toList();


        List<BrandMedicationsDTO> medications =
                patientPrescriptionRepository
                        .findByEncounterIdOrderByCreatedDateAsc(encounterId)
                        .stream()
                        .filter(rx -> rx.getStatus() != PrescriptionStatus.CANCELLED)
                        .flatMap(rx ->
                                patientPrescriptionMedicationRepository
                                        .findByPrescriptionHeader_IdOrderByIdAsc(rx.getId())
                                        .stream()
                                        .filter(m -> m.getStatus() != PrescriptionStatus.CANCELLED)
                                        .map(m -> {
                                            BrandMedication brand = m.getMedications();
                                            return new BrandMedicationsDTO(
                                                    brand != null ? brand.getName() : null,
                                                    brand != null ? brand.getCode() : null,
                                                    m.getInstructions(),
                                                    m.getInstructionsType() != null ? m.getInstructionsType().name() : null
                                            );
                                        })
                        )
                        .toList();


        List<ProceduresDTO> procedures = rawPatientProcedures
                .stream()
                .map(p -> {
                    Procedure proc = procedureRepository
                            .findById(p.getProcedureId())
                            .orElse(null);
                    return new ProceduresDTO(
                            proc != null ? proc.getName() : null,
                            proc != null ? proc.getCode() : null,
                            proc != null ? resolve(lovMap, proc.getCategoryType()) : null,
                            p.getNotes()
                    );
                })
                .toList();

        System.out.println("Procedures size = " + procedures.size());

        return new NurseSummaryReportDTO(
                patientInfo,
                encounterInfo,
                observation,
                vitalSigns,
                bodyMeasurements,
                additionalMeasurements,
                allergies,
                warnings,
                vaccinations,
                servicesAndProducts,
                diagnosticsOrder,
                medications,
                procedures,
                Instant.now()
        );
    }

    private NurseSummaryPatientInfoDTO mapPatient(Patient patient) {
        String fullName = buildFullName(
                patient.getFirstName(),
                patient.getSecondName(),
                patient.getThirdName(),
                patient.getLastName()
        );

        Integer age = null;
        if (patient.getDateOfBirth() != null) {
            age = Period.between(
                    patient.getDateOfBirth().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate(),
                    LocalDate.now()
            ).getYears();
        }

        return new NurseSummaryPatientInfoDTO(
                patient.getId(),
                fullName,
                patient.getMedicalRecordNumber(),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth().toInstant() : null,
                age,
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : null
        );
    }

    private NurseSummaryEncounterInfoDTO mapEncounter(PatientEncounter encounter) {
        PatientEncounter fresh = encounterRepository.findById(encounter.getId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Encounter not found with id " + encounter.getId(),
                        "patientEncounters",
                        "notfound"
                ));

        Department department = departmentsRepository.findById(fresh.getDepartmentId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Department not found with id " + fresh.getDepartmentId(),
                        "departments",
                        "notfound"
                ));

        String facilityName = department.getFacility() != null
                ? department.getFacility().getName()
                : null;

        return new NurseSummaryEncounterInfoDTO(
                fresh.getId(),
                fresh.getEncounterNumber(),
                fresh.getEncounterDate(),
                fresh.getEncounterType() != null ? fresh.getEncounterType().name() : null,
                fresh.getEncounterReason() != null ? fresh.getEncounterReason().name() : null,
                fresh.getPriorityLevel() != null ? fresh.getPriorityLevel().name() : null,
                fresh.getStatus() != null ? fresh.getStatus().name() : null,
                fresh.getChiefComplaint(),
                facilityName,
                department.getName(),
                fresh.getCreatedDate()
        );
    }

    private NurseSummaryObservationDTO mapObservation(
            PatientObservationsComplaints entity,
            String primaryDiagnosis,
            String plan
    ) {
        return new NurseSummaryObservationDTO(
                entity.getReasonOfVisit(),
                entity.getFunctionalStatus(),
                entity.getPatientConditions(),
                entity.getCognitiveCheck(),
                primaryDiagnosis,
                plan
        );
    }

    private NurseSummaryBodyMeasurementsDTO mapBodyMeasurements(BodyMeasurements entity) {
        return new NurseSummaryBodyMeasurementsDTO(
                entity.getWeight(),
                entity.getHeight(),
                entity.getHeadCircumference()
        );
    }

    private NurseSummaryAdditionalMeasurementsDTO mapAdditionalMeasurements(
            AdditionalMeasurements entity
    ) {
        return new NurseSummaryAdditionalMeasurementsDTO(
                entity.getAgeGroup() != null ? entity.getAgeGroup().name() : null,
                entity.getHearingTest(),
                entity.getDehydration(),
                entity.getNasalFlaring(),
                entity.getResponseToLight(),
                entity.getPupilResponse(),
                entity.getAbilityToFollowTarget(),
                entity.getColorTesting(),
                entity.getFallRisk(),
                entity.getVisionProblemsAffectingFunction(),
                entity.getHearingProblemsAffectingFunction(),
                entity.getDetails(),
                entity.getActionToTake()
        );
    }

    private NurseSummaryAllergyDTO mapAllergy(PatientAllergies entity, Map<String, String> lovMap) {
        return new NurseSummaryAllergyDTO(
                entity.getId(),
                resolve(lovMap, entity.getAllergenType() != null ? entity.getAllergenType().name() : null),
                entity.getAllergen(),
                entity.getSeverity() != null ? entity.getSeverity().name() : null,
                resolve(lovMap, entity.getCriticality()),
                entity.getCertainty(),
                entity.getTreatmentStrategy(),
                resolve(lovMap, entity.getOnset()),
                entity.getOnsetDate(),
                entity.getTypeOfPropensity(),
                entity.isByPatient(),
                entity.getSourceOfInformation(),
                resolve(lovMap, entity.getAllergicReactions()),
                entity.getNote(),
                entity.getStatus() != null ? entity.getStatus().name() : null
        );
    }

    private NurseSummaryWarningDTO mapWarning(PatientWarnings entity, Map<String, String> lovMap) {
        return new NurseSummaryWarningDTO(
                entity.getId(),
                resolve(lovMap, entity.getWarningType()),
                entity.getWarning(),
                entity.getSeverity() != null ? entity.getSeverity().name() : null,
                entity.getOnsetDate(),
                entity.isByPatient(),
                entity.getSourceOfInformation(),
                entity.getNote(),
                entity.getActionTaken(),
                entity.getStatus() != null ? entity.getStatus().name() : null
        );
    }

    private NurseSummaryVaccinationDTO mapVaccination(EncounterVaccination entity) {
        return new NurseSummaryVaccinationDTO(
                entity.getId(),
                entity.getVaccineId(),
                entity.getVaccineBrandId(),
                entity.getVaccineDoseId(),
                entity.getVaccineLotNumber(),
                entity.getDateAdministered(),
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getAdministeredLocation(),
                entity.getAdministrationReactions(),
                entity.getIsExternalFacility(),
                entity.getExternalFacilityName(),
                entity.getNotes()
        );
    }

    private NurseSummaryServiceProductDTO mapServiceAndProduct(
            PatientServiceAndProduct entity,
            Map<String, String> lovMap
    ) {
        String categoryKey = entity.getCategory() != null ? entity.getCategory().name() : null;
        String categoryDisplay = resolve(lovMap, categoryKey);

        return new NurseSummaryServiceProductDTO(
                entity.getId(),
                categoryDisplay,
                entity.getServiceId() != null ? entity.getServiceId() : entity.getProductId(),
                null,
                entity.getQuantity(),
                null,
                null,
                entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : null
        );
    }

    private String buildFullName(String first, String second, String third, String last) {
        return String.join(" ", safe(first), safe(second), safe(third), safe(last)).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}