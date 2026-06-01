package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.enumeration.DiagnosisType;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import com.dazzle.asklepios.service.dto.reports.SickLeaveReportDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SickLeaveReportService {

    private static final Logger LOG = LoggerFactory.getLogger(SickLeaveReportService.class);

    private final NurseSummaryReportService nurseSummaryReportService;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final PatientEncounterRepository patientEncounterRepository;
    private final PractitionersRepository practitionersRepository;
    private final UserRepository userRepository;

    public SickLeaveReportDTO getSickLeaveReport(Long encounterId,
                                                  LocalDate sickLeaveFromDate,
                                                  LocalDate sickLeaveToDate,
                                                  String notes) {
        LOG.debug("[SICK_LEAVE] start encounterId={}", encounterId);

        NurseSummaryReportDTO nurseSummary =
                nurseSummaryReportService.getNurseSummaryReport(encounterId);

        if (nurseSummary == null) {
            return null;
        }

        String diagnosis = patientDiagnosisRepository
                .findByEncounterIdAndType(encounterId, DiagnosisType.PRIMARY)
                .map(diag -> diag.getDiagnosis().getIcdShortDescription())
                .orElse(null);

        Integer numberOfDays = null;
        if (sickLeaveFromDate != null && sickLeaveToDate != null
                && !sickLeaveToDate.isBefore(sickLeaveFromDate)) {
            numberOfDays = (int) ChronoUnit.DAYS.between(sickLeaveFromDate, sickLeaveToDate) + 1;
        }

        String physicianFullName = null;
        String physicianSpecialty = null;

        Long practitionerId = patientEncounterRepository.findById(encounterId)
                .map(com.dazzle.asklepios.domain.PatientEncounter::getPractitionerId)
                .orElse(null);

        if (practitionerId != null) {
            Practitioner practitioner = practitionersRepository.findById(practitionerId).orElse(null);
            if (practitioner != null) {
                physicianFullName = practitioner.getFirstName() + " " + practitioner.getLastName();
                physicianSpecialty = practitioner.getSpecialty() != null
                        ? practitioner.getSpecialty()
                        : null;
            }
        }

        // Append current authenticated user's full name (if available) to attending physician info
        java.util.Optional<String> currentUser = SecurityUtils.getCurrentUserLogin();
        if (currentUser.isPresent()) {
            String username = currentUser.get();
            String displayName = username;
            try {
                displayName = userRepository.findByLogin(username)
                        .map(u -> {
                            String fn = u.getFirstName() != null ? u.getFirstName().trim() : "";
                            String ln = u.getLastName() != null ? u.getLastName().trim() : "";
                            String full = (fn + " " + ln).trim();
                            return full.isEmpty() ? username : full;
                        })
                        .orElse(username);
            } catch (Exception e) {
                // fallback to username if repository lookup fails
                displayName = username;
            }

            if (physicianFullName == null) {
                physicianFullName = displayName;
            } else {
                physicianFullName = physicianFullName + " — " + displayName;
            }
        }

        return new SickLeaveReportDTO(
                nurseSummary.patientInfo(),
                nurseSummary.encounterInfo(),
                diagnosis,
                notes,
                sickLeaveFromDate,
                sickLeaveToDate,
                numberOfDays,
                physicianFullName,
                physicianSpecialty,
                Instant.now()
        );
    }

    // Backward-compatible overload: delegate to new method without notes
    public SickLeaveReportDTO getSickLeaveReport(Long encounterId,
                                                  LocalDate sickLeaveFromDate,
                                                  LocalDate sickLeaveToDate) {
        return getSickLeaveReport(encounterId, sickLeaveFromDate, sickLeaveToDate, null);
    }
}
