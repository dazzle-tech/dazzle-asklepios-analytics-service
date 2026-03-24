package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.AdditionalMeasurements;
import com.dazzle.asklepios.domain.BodyMeasurements;
import com.dazzle.asklepios.domain.EncounterVaccination;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientObservationsComplaints;
import com.dazzle.asklepios.domain.PatientServiceAndProduct;
import com.dazzle.asklepios.domain.PatientWarnings;
import com.dazzle.asklepios.domain.VitalSigns;
import com.dazzle.asklepios.domain.enumeration.EncounterVaccinationStatus;
import com.dazzle.asklepios.domain.enumeration.PatientAllergyStatus;
import com.dazzle.asklepios.domain.enumeration.PatientWarningStatus;
import com.dazzle.asklepios.repository.EncounterVaccinationRepository;
//import com.dazzle.asklepios.repository.PatientAllergiesRepository;
import com.dazzle.asklepios.repository.PatientAllergiesRepository;
import com.dazzle.asklepios.repository.PatientServiceAndProductRepository;
import com.dazzle.asklepios.repository.PatientWarningsRepository;
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
import java.util.List;

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

    private final PatientAllergiesRepository patientAllergiesRepository;
    private final PatientWarningsRepository patientWarningsRepository;
    private final EncounterVaccinationRepository encounterVaccinationRepository;
    private final PatientServiceAndProductRepository patientServiceAndProductRepository;

    public NurseSummaryReportDTO getNurseSummaryReport(Long encounterId) {
        LOG.debug("[NURSE_SUMMARY] start encounterId={}", encounterId);

        PatientEncounter encounter = patientEncounterService.getById(encounterId);
        Patient patient = encounter.getPatient();

        NurseSummaryPatientInfoDTO patientInfo = mapPatient(patient);
        NurseSummaryEncounterInfoDTO encounterInfo = mapEncounter(encounter);

        NurseSummaryObservationDTO observation = patientObservationsComplaintsService
                .findLatestByEncounterId(encounterId)
                .map(this::mapObservation)
                .orElse(null);

        NurseSummaryVitalSignsDTO vitalSigns = vitalSignsService
                .findLatestByEncounterId(encounterId)
                .map(this::mapVitalSigns)
                .orElse(null);

        NurseSummaryBodyMeasurementsDTO bodyMeasurements = bodyMeasurementsService
                .findLatestByEncounterId(encounterId)
                .map(this::mapBodyMeasurements)
                .orElse(null);

        NurseSummaryAdditionalMeasurementsDTO additionalMeasurements = additionalMeasurementsService
                .findLatestByEncounterId(encounterId)
                .map(this::mapAdditionalMeasurements)
                .orElse(null);

        List<NurseSummaryAllergyDTO> allergies =
                ((List<PatientAllergies>) patientAllergiesRepository
                        .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
                                encounterId,
                                PatientAllergyStatus.CANCELLED
                        ))
                        .stream()
                        .map(this::mapAllergy)
                        .toList();

        List<NurseSummaryWarningDTO> warnings = patientWarningsRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(encounterId, PatientWarningStatus.CANCELLED)
                .stream()
                .map(this::mapWarning)
                .toList();

        List<NurseSummaryVaccinationDTO> vaccinations = encounterVaccinationRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(encounterId, EncounterVaccinationStatus.CANCELLED)
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
                    patient.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
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
        return new NurseSummaryEncounterInfoDTO(
                encounter.getId(),
                encounter.getEncounterNumber(),
                encounter.getEncounterDate(),
                encounter.getEncounterType() != null ? encounter.getEncounterType().name() : null,
                encounter.getEncounterReason() != null ? encounter.getEncounterReason().name() : null,
                encounter.getPriorityLevel() != null ? encounter.getPriorityLevel().name() : null,
                encounter.getStatus() != null ? encounter.getStatus().name() : null,
                encounter.getChiefComplaint(),
                encounter.getFacilityId(),
                encounter.getDepartmentId(),
                encounter.getCreatedDate()
        );
    }

    private NurseSummaryObservationDTO mapObservation(PatientObservationsComplaints entity) {
        return new NurseSummaryObservationDTO(
                entity.getReasonOfVisit(),
                entity.getFunctionalStatus(),
                entity.getPatientConditions(),
                entity.getCognitiveCheck()
        );
    }

    private NurseSummaryVitalSignsDTO mapVitalSigns(VitalSigns entity) {
        return new NurseSummaryVitalSignsDTO(
                entity.getBloodPressureSystolic(),
                entity.getBloodPressureDiastolic(),
                entity.getMeasurementSite(),
                entity.getHeartRate(),
                entity.getTemperature(),
                entity.getOxygenSaturation(),
                entity.getRespiratoryRate(),
                entity.getNotes()
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
                entity.getAllergenId(),
                entity.getSeverity() != null ? entity.getSeverity().name() : null,
                entity.getCriticality(),
                entity.getCertainty(),
                entity.getTreatmentStrategy(),
                entity.getOnset(),
                entity.getOnsetDate(),
                entity.getTypeOfPropensity(),
                entity.isByPatient(),
                entity.getSourceOfInformation(),
                entity.getAllergicReactions(),
                entity.getNote(),
                entity.getStatus() != null ? entity.getStatus().name() : null
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
                entity.getCategory() != null ? entity.getCategory().name() : null,
                entity.getServiceId(),
                entity.getProductId(),
                entity.getQuantity()
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