package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.AdditionalMeasurements;
import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.BodyMeasurements;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.EncounterVaccination;
import com.dazzle.asklepios.domain.PainAssessment;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientObservationsComplaints;
import com.dazzle.asklepios.domain.PatientServiceAndProduct;
import com.dazzle.asklepios.domain.PatientWarnings;
import com.dazzle.asklepios.domain.VitalSigns;
import com.dazzle.asklepios.domain.enumeration.DiagnosisType;
import com.dazzle.asklepios.domain.enumeration.EncounterVaccinationStatus;
import com.dazzle.asklepios.domain.enumeration.PatientAllergyStatus;
import com.dazzle.asklepios.domain.enumeration.PatientWarningStatus;
import com.dazzle.asklepios.repository.AdditionalMeasurementsRepository;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.BodyMeasurementsRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.EncounterVaccinationRepository;
import com.dazzle.asklepios.repository.PainAssessmentRepository;
import com.dazzle.asklepios.repository.PatientAllergiesRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientObservationsComplaintsRepository;
import com.dazzle.asklepios.repository.PatientServiceAndProductRepository;
import com.dazzle.asklepios.repository.PatientWarningsRepository;
import com.dazzle.asklepios.repository.VitalSignsRepository;
import com.dazzle.asklepios.service.dto.painAssessment.PainAssessmentDTO;
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
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NurseSummaryReportService {

    private static final Logger LOG = LoggerFactory.getLogger(NurseSummaryReportService.class);

    private final PatientEncounterRepository patientEncounterRepository;
    private final PatientObservationsComplaintsRepository patientObservationsComplaintsRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final BodyMeasurementsRepository bodyMeasurementsRepository;
    private final AdditionalMeasurementsRepository additionalMeasurementsRepository;
    private final DepartmentsRepository departmentsRepository;
    private final PatientAllergiesRepository patientAllergiesRepository;
    private final PatientWarningsRepository patientWarningsRepository;
    private final EncounterVaccinationRepository encounterVaccinationRepository;
    private final PatientServiceAndProductRepository patientServiceAndProductRepository;
    private final PatientEncounterRepository encounterRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final PainAssessmentRepository painAssessmentRepository;
    private final ApLovValueRepository apLovValueRepository;

    public NurseSummaryReportDTO getNurseSummaryReport(Long encounterId) {
        LOG.debug("[NURSE_SUMMARY] start encounterId={}", encounterId);

        PatientEncounter encounter = patientEncounterRepository
                .findById(encounterId)
                .orElse(null);

        Patient patient = encounter != null ? encounter.getPatient() : null;

        NurseSummaryPatientInfoDTO patientInfo =
                patient != null ? mapPatient(patient) : null;

        NurseSummaryEncounterInfoDTO encounterInfo =
                encounter != null ? mapEncounter(encounter) : null;

        NurseSummaryObservationDTO observation = patientObservationsComplaintsRepository
                .findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(encounterId)
                .map(this::mapObservation)
                .orElse(null);

        String primaryDiagnosis = patientDiagnosisRepository
                .findByEncounterIdAndType(encounterId, DiagnosisType.PRIMARY)
                .map(diag -> diag.getDiagnosis().getIcdShortDescription())
                .orElse("Not Found");

        if (observation != null) {
            observation = new NurseSummaryObservationDTO(
                    observation.reasonOfVisit(),
                    observation.functionalStatus(),
                    observation.patientConditions(),
                    observation.cognitiveCheck(),
                    primaryDiagnosis,
                    null
            );
        } else {
            observation = new NurseSummaryObservationDTO(
                    null,
                    null,
                    null,
                    null,
                    primaryDiagnosis,
                    null
            );
        }

        NurseSummaryVitalSignsDTO vitalSigns = vitalSignsRepository
                .findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(encounterId)
                .map(this::mapVitalSigns)
                .orElse(null);

        NurseSummaryBodyMeasurementsDTO bodyMeasurements = bodyMeasurementsRepository
                .findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(encounterId)
                .map(this::mapBodyMeasurements)
                .orElse(null);

        NurseSummaryAdditionalMeasurementsDTO additionalMeasurements = additionalMeasurementsRepository
                .findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(encounterId)
                .map(this::mapAdditionalMeasurements)
                .orElse(null);

        PainAssessmentDTO painAssessmentDTO =
                painAssessmentRepository
                        .findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(encounterId)
                        .map(this::mapPainAssessment)
                        .orElse(null);
        List<NurseSummaryAllergyDTO> allergies = patientAllergiesRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
                        encounterId,
                        PatientAllergyStatus.CANCELLED
                )
                .stream()
                .map(this::mapAllergy)
                .toList();

        List<NurseSummaryWarningDTO> warnings = patientWarningsRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
                        encounterId,
                        PatientWarningStatus.CANCELLED
                )
                .stream()
                .map(this::mapWarning)
                .toList();

        List<NurseSummaryVaccinationDTO> vaccinations = encounterVaccinationRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
                        encounterId,
                        EncounterVaccinationStatus.CANCELLED
                )
                .stream()
                .map(this::mapVaccination)
                .toList();

        List<NurseSummaryServiceProductDTO> servicesAndProducts = patientServiceAndProductRepository
                .findByEncounterIdOrderByCreatedDateAsc(encounterId)
                .stream()
                .map(this::mapServiceAndProduct)
                .toList();

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
                Instant.now(),
                painAssessmentDTO
        );
    }

    private String calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }

        LocalDate birthDate = dateOfBirth.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate today = LocalDate.now();
        Period period = Period.between(birthDate, today);

        return period.getYears() + " Years " +
                period.getMonths() + " Months " +
                period.getDays() + " Days";
    }

    private NurseSummaryPatientInfoDTO mapPatient(Patient patient) {
        String fullName = buildFullName(
                patient.getFirstName(),
                patient.getSecondName(),
                patient.getThirdName(),
                patient.getLastName()
        );


        return new NurseSummaryPatientInfoDTO(
                patient.getId(),
                fullName,
                patient.getMedicalRecordNumber(),
                patient.getDateOfBirth(),
                calculateAge(patient.getDateOfBirth()),
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : null
        );
    }

    private NurseSummaryEncounterInfoDTO mapEncounter(PatientEncounter encounter) {

        PatientEncounter freshEncounter = encounterRepository.findById(encounter.getId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Encounter not found with id " + encounter.getId(),
                        "patientEncounters",
                        "notfound"
                ));

        Department department = departmentsRepository.findById(freshEncounter.getDepartmentId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Department not found with id " + freshEncounter.getDepartmentId(),
                        "departments",
                        "notfound"
                ));

        String facilityName = department.getFacility() != null
                ? department.getFacility().getName()
                : null;

        String departmentName = department.getName();

        return new NurseSummaryEncounterInfoDTO(
                freshEncounter.getId(),
                freshEncounter.getEncounterNumber(),
                freshEncounter.getEncounterDate(),
                freshEncounter.getEncounterReason() != null ? freshEncounter.getEncounterReason().name() : null,
                freshEncounter.getPriorityLevel() != null ? freshEncounter.getPriorityLevel().name() : null,
                freshEncounter.getStatus() != null ? freshEncounter.getStatus().name() : null,
                freshEncounter.getChiefComplaint(),
                facilityName,
                departmentName,
                freshEncounter.getCreatedDate()
        );


    }


    private NurseSummaryObservationDTO mapObservation(PatientObservationsComplaints entity) {
        return new NurseSummaryObservationDTO(
                entity.getReasonOfVisit(),
                entity.getFunctionalStatus(),
                entity.getPatientConditions(),
                entity.getCognitiveCheck(),
                null,
                null
        );
    }

    private NurseSummaryVitalSignsDTO mapVitalSigns(VitalSigns entity) {
        return new NurseSummaryVitalSignsDTO(
                entity.getBloodPressureSystolic(),
                entity.getBloodPressureDiastolic(),
                apLovValueRepository.findById(entity.getMeasurementSite())
                        .map(ApLovValue::getLovDisplayVale)
                        .orElse(null),
                entity.getHeartRate(),
                entity.getTemperature(),
                entity.getOxygenSaturation(),
                entity.getRespiratoryRate(),
                entity.getNotes(),
                null
        );
    }

    private NurseSummaryBodyMeasurementsDTO mapBodyMeasurements(BodyMeasurements entity) {
        return new NurseSummaryBodyMeasurementsDTO(
                entity.getWeight(),
                entity.getHeight(),
                entity.getHeadCircumference()
        );
    }

    private NurseSummaryAdditionalMeasurementsDTO mapAdditionalMeasurements(AdditionalMeasurements entity) {
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

    private NurseSummaryAllergyDTO mapAllergy(PatientAllergies entity) {
        return new NurseSummaryAllergyDTO(
                entity.getId(),
                entity.getAllergenType() != null ? entity.getAllergenType().name() : null,
                entity.getAllergen().getName(),
                entity.getSeverity()

        );
    }

    private NurseSummaryWarningDTO mapWarning(PatientWarnings entity) {
        return new NurseSummaryWarningDTO(
                entity.getId(),
                entity.getWarningType(),
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

    private NurseSummaryServiceProductDTO mapServiceAndProduct(PatientServiceAndProduct entity) {
        return new NurseSummaryServiceProductDTO(
                entity.getId(),
                entity.getServiceId(),
                entity.getQuantity(),
//                entity.getName(),
//                entity.getCode(),
//                entity.getUnit(),
//                entity.getNotes(),
                null,
                null,
                null,
                null,
                entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : null
        );
    }

    private PainAssessmentDTO mapPainAssessment(PainAssessment entity) {
        return new PainAssessmentDTO(
                entity.getPainDegree(),
                entity.getPainLevel(),
                apLovValueRepository.findById(entity.getPainPattern())
                        .map(ApLovValue::getLovDisplayVale)
                        .orElse(null),
                entity.getPainDescription()
        );
    }

    private String buildFullName(String first, String second, String third, String last) {
        return String.join(" ",
                safe(first),
                safe(second),
                safe(third),
                safe(last)
        ).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}