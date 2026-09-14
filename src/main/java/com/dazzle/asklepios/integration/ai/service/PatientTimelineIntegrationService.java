package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.CurrentMedication;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientDiagnosis;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientProcedure;
import com.dazzle.asklepios.domain.VitalSigns;
import com.dazzle.asklepios.domain.enumeration.PatientHistoryStatus;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.integration.ai.client.PatientTimelineClient;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.AllergyEntryDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.DemographicsDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.DiagnosisEntryDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.EncounterEntryDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.LabResultEntryDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.MedicationEntryDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.PatientDataInputDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.ProcedureEntryDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.TimelineRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.TimelineResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.timeline.VitalEntryDTO;
import com.dazzle.asklepios.repository.CurrentMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestResultRepository;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.repository.PatientAllergiesRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.VitalSignsRepository;
import com.dazzle.asklepios.service.LovLookupService;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientTimelineIntegrationService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientTimelineIntegrationService.class);

    private final PatientTimelineClient patientTimelineClient;
    private final PatientRepository patientRepository;
    private final PatientEncounterRepository patientEncounterRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final CurrentMedicationRepository currentMedicationRepository;
    private final PatientAllergiesRepository patientAllergiesRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final PatientProcedureRepository patientProcedureRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticOrderTestResultRepository diagnosticOrderTestResultRepository;
    private final DiagnosticTestProfileRepository diagnosticTestProfileRepository;
    private final LovLookupService lovLookupService;

    public TimelineResponseDTO generateTimeline(Long patientId) {
        LOG.debug("[PATIENT_TIMELINE] generating timeline for patientId={}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException("Patient not found with id " + patientId, "patient", "notfound"));

        PatientDataInputDTO patientData = buildPatientData(patient);

        TimelineRequestDTO request = new TimelineRequestDTO(
                UUID.randomUUID().toString(),
                patientData
        );
        return  patientTimelineClient.generateTimeline(request);
    }

    private PatientDataInputDTO buildPatientData(Patient patient) {
        List<PatientEncounter> encounters = patientEncounterRepository.findByPatient_IdOrderByEncounterDateDesc(patient.getId());
        List<CurrentMedication> medications = currentMedicationRepository.findByPatientIdAndStatus(patient.getId(), PatientHistoryStatus.ACTIVE);

        List<PatientDiagnosis> diagnoses = patientDiagnosisRepository.findByPatientId(patient.getId());
        List<PatientAllergies> allergies = patientAllergiesRepository.findByPatientIdOrderByCreatedDateAsc(patient.getId());
        List<VitalSigns> vitals = vitalSignsRepository.findByPatient_IdAndIsActiveTrueOrderByCreatedDateAsc(patient.getId());
        List<PatientProcedure> procedures = patientProcedureRepository.findByPatient_IdOrderByCreatedDateAsc(patient.getId());
        List<DiagnosticOrderTestResult> labResults = fetchLabResults(patient.getId());

        return new PatientDataInputDTO(
                String.valueOf(patient.getId()),
                buildDemographics(patient),
                buildDiagnoses(diagnoses),
                buildMedications(medications),
                buildLabResults(labResults),
                buildVitals(vitals),
                buildProcedures(procedures),
                buildEncounters(encounters),
                buildAllergies(allergies)
        );
    }

    private DemographicsDTO buildDemographics(Patient patient) {
        String age = patient.getDateOfBirth() != null ? buildAge(patient.getDateOfBirth()) : null;
        String gender = patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : null;
        return new DemographicsDTO(age, gender);
    }

    private List<DiagnosisEntryDTO> buildDiagnoses(List<PatientDiagnosis> diagnoses) {
        return diagnoses.stream()
                .filter(d -> d.getDiagnosis() != null)
                .map(d -> new DiagnosisEntryDTO(
                        d.getDiagnosis().getIcdShortDescription(),
                        toDateString(d.getCreatedDate())
                ))
                .collect(Collectors.toList());
    }

    private List<MedicationEntryDTO> buildMedications(List<CurrentMedication> medications) {
        return medications.stream()
                .map(m -> new MedicationEntryDTO(
                        m.getActiveIngredient() != null ? m.getActiveIngredient().getName() : "Unknown",
                        m.getStartDate() != null
                                ? Instant.ofEpochMilli(m.getStartDate().getTime())
                                .atZone(ZoneId.systemDefault()).toLocalDate().toString()
                                : "",
                        null,
                        m.getStatus() != null ? m.getStatus().name() : null
                ))
                .collect(Collectors.toList());
    }

    private List<EncounterEntryDTO> buildEncounters(List<PatientEncounter> encounters) {
        return encounters.stream()
                .map(e -> new EncounterEntryDTO(
                        e.getEncounterType() != null ? e.getEncounterType().name() : "Unknown",
                        e.getEncounterDate() != null ? e.getEncounterDate().toString() : "",
                        e.getChiefComplaint()
                ))
                .collect(Collectors.toList());
    }

    private List<VitalEntryDTO> buildVitals(List<VitalSigns> vitals) {
        List<VitalEntryDTO> entries = new ArrayList<>();
        for (VitalSigns v : vitals) {
            String date = toDateString(v.getCreatedDate());

            if (v.getBloodPressureSystolic() != null && v.getBloodPressureDiastolic() != null) {
                entries.add(new VitalEntryDTO("Blood Pressure",
                        v.getBloodPressureSystolic() + "/" + v.getBloodPressureDiastolic() + " mmHg", date));
            }
            if (v.getHeartRate() != null) {
                entries.add(new VitalEntryDTO("Heart Rate", v.getHeartRate() + " bpm", date));
            }
            if (v.getTemperature() != null) {
                entries.add(new VitalEntryDTO("Temperature", v.getTemperature() + " °C", date));
            }
            if (v.getOxygenSaturation() != null) {
                entries.add(new VitalEntryDTO("Oxygen Saturation", v.getOxygenSaturation() + "%", date));
            }
            if (v.getRespiratoryRate() != null) {
                entries.add(new VitalEntryDTO("Respiratory Rate", v.getRespiratoryRate() + " breaths/min", date));
            }
        }
        return entries;
    }

    private List<DiagnosticOrderTestResult> fetchLabResults(Long patientId) {
        List<Long> orderIds = diagnosticOrderRepository.findByPatientId(patientId).stream()
                .map(DiagnosticOrder::getId)
                .toList();
        if (orderIds.isEmpty()) {
            return List.of();
        }

        List<Long> orderTestIds = diagnosticOrderTestRepository.findByOrderIdIn(orderIds).stream()
                .map(DiagnosticOrderTest::getId)
                .toList();
        if (orderTestIds.isEmpty()) {
            return List.of();
        }

        return diagnosticOrderTestResultRepository.findByOrderTestIdIn(orderTestIds);
    }

    private List<LabResultEntryDTO> buildLabResults(List<DiagnosticOrderTestResult> labResults) {
        return labResults.stream()
                .map(r -> {
                    var profile = diagnosticTestProfileRepository.findById(r.getProfileTestId()).orElse(null);
                    String name = profile != null ? profile.getName() : "Unknown";
                    String unit = resolveUnit(profile);
                    String value;
                    if (r.getResultValueNumber() != null) {
                        value = r.getResultValueNumber().toString();
                    } else if (profile != null && profile.getResultType() == TestResultType.LOV) {
                        String displayValue = lovLookupService.findDisplayValue(r.getResultValueText());
                        value = displayValue != null ? displayValue : r.getResultValueText();
                    } else {
                        value = r.getResultValueText();
                    }
                    String date = r.getApprovedDate() != null ? toDateString(r.getApprovedDate()) : toDateString(r.getCreatedDate());
                    String flag = r.getMarker() != null ? r.getMarker().name() : null;
                    return value != null && !value.isBlank()
                            ? new LabResultEntryDTO(name, value, unit, date, flag)
                            : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String resolveUnit(DiagnosticTestProfile profile) {
        if (profile == null || profile.getResultUnit() == null || profile.getResultUnit().isBlank()) {
            return null;
        }
        String displayValue = lovLookupService.findDisplayValue(profile.getResultUnit());
        return displayValue != null ? displayValue : profile.getResultUnit();
    }

    private List<ProcedureEntryDTO> buildProcedures(List<PatientProcedure> procedures) {
        return procedures.stream()
                .map(p -> new ProcedureEntryDTO(
                        p.getProcedure() != null ? p.getProcedure().getName() : "Unknown",
                        p.getScheduledDateTime() != null ? toDateString(p.getScheduledDateTime()) : "",
                        p.getStatus()
                ))
                .collect(Collectors.toList());
    }

    private List<AllergyEntryDTO> buildAllergies(List<PatientAllergies> allergies) {
        return allergies.stream()
                .map(a -> {
                    String name = resolveAllergyName(a);
                    return name != null && !name.isBlank()
                            ? new AllergyEntryDTO(name, toDateString(a.getCreatedDate()))
                            : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String resolveAllergyName(PatientAllergies allergy) {
        if (allergy.getAllergen() != null) {
            return allergy.getAllergen().getName();
        }
        if (allergy.getAllergenName() != null && !allergy.getAllergenName().isBlank()) {
            return allergy.getAllergenName();
        }
        if (allergy.getMedicationClass() != null) {
            return allergy.getMedicationClass().getName();
        }
        return null;
    }

    private String buildAge(Date dateOfBirth) {
        LocalDate birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Period period = Period.between(birthDate, LocalDate.now());
        return period.getYears() + "y " + period.getMonths() + "m " + period.getDays() + "d";
    }

    private String toDateString(Instant instant) {
        return instant != null ? instant.atZone(ZoneId.systemDefault()).toLocalDate().toString() : "";
    }
}
