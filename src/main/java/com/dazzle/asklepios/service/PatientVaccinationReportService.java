package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.enumeration.EncounterVaccinationStatus;
import com.dazzle.asklepios.repository.EncounterVaccinationRepository;
import com.dazzle.asklepios.repository.IdValueProjection;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.PatientVaccinationReportProjection;
import com.dazzle.asklepios.service.dto.reports.vaccination.PatientVaccinationReportDTO;
import com.dazzle.asklepios.service.dto.reports.vaccination.VaccinationReportDoseDTO;
import com.dazzle.asklepios.service.dto.reports.vaccination.VaccinationReportVaccineDTO;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PatientVaccinationReportService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientVaccinationReportService.class);

    private static final List<String> NUMBER_OF_DOSES = List.of(
            "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN"
    );

    private static final List<String> DOSE_NUMBERS = List.of(
            "FIRST", "SECOND", "THIRD", "FOURTH", "FIFTH", "SIXTH", "SEVENTH", "EIGHTH", "NINTH", "TENTH"
    );

    private static final Map<String, String> ROUTES_OF_ADMINISTRATION = Map.of(
            "RECTALLY", "Rectally",
            "ORALLY_PO", "Orally (PO)",
            "INTRAMUSCULAR_IM", "Intramuscular (IM)",
            "SUBCUTANEOUS_SC", "Subcutaneous (SC)",
            "INTRADERMAL_ID", "Intradermal (ID)",
            "NASAL_IN", "Nasal (IN)",
            "INTRAVENOUS_IV", "Intravenous (IV)",
            "TRANSDERMAL_PATCH", "Transdermal Patch",
            "TOPICAL", "Topical"
    );

    private final PatientRepository patientRepository;
    private final EncounterVaccinationRepository encounterVaccinationRepository;
    private final ReportCommonService reportCommonService;

    @Transactional(readOnly = true)
    public PatientVaccinationReportDTO getPatientVaccinationReport(Long patientId) {

        LOG.debug("[PatientVaccinationReport] GET start patientId={}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Patient not found with id " + patientId,
                        "patient",
                        "notfound"
                ));

        List<PatientVaccinationReportProjection> rows =
                encounterVaccinationRepository.findPatientVaccinationReportRows(
                        patientId,
                        EncounterVaccinationStatus.CANCELLED.name()
                );

        Map<Long, String> brandNames = toValueMap(
                encounterVaccinationRepository::findVaccineBrandNamesByIds,
                rows.stream().map(PatientVaccinationReportProjection::getVaccineBrandId)
        );

        Map<Long, String> doseNumbers = toValueMap(
                encounterVaccinationRepository::findVaccineDoseNumbersByIds,
                rows.stream().map(PatientVaccinationReportProjection::getVaccineDoseId)
        );

        Map<Long, VaccinationReportVaccineDTO> vaccines = new LinkedHashMap<>();

        for (PatientVaccinationReportProjection row : rows) {
            VaccinationReportVaccineDTO vaccine = vaccines.computeIfAbsent(
                    row.getVaccineId(),
                    id -> new VaccinationReportVaccineDTO(
                            id,
                            row.getVaccineName(),
                            row.getAtcCode(),
                            reportCommonService.formatEnum(row.getVaccineType()),
                            enumToNumber(NUMBER_OF_DOSES, row.getNumberOfDoses()),
                            formatRoa(row.getRoa()),
                            row.getSiteOfAdministration(),
                            new ArrayList<>()
                    )
            );

            vaccine.doses().add(new VaccinationReportDoseDTO(
                    brandNames.get(row.getVaccineBrandId()),
                    enumToNumber(DOSE_NUMBERS, doseNumbers.get(row.getVaccineDoseId())),
                    row.getDateAdministered(),
                    resolveLocation(row)
            ));
        }

        LOG.debug(
                "[PatientVaccinationReport] GET completed patientId={} vaccines={} doses={}",
                patientId,
                vaccines.size(),
                rows.size()
        );

        return new PatientVaccinationReportDTO(
                patient.getId(),
                reportCommonService.getPatientDisplayName(patient),
                patient.getMedicalRecordNumber(),
                patient.getSexAtBirth() != null
                        ? reportCommonService.formatEnum(patient.getSexAtBirth().name())
                        : null,
                patient.getDateOfBirth() != null
                        ? new SimpleDateFormat("dd/MM/yyyy").format(patient.getDateOfBirth())
                        : null,
                reportCommonService.calculateAge(patient.getDateOfBirth()),
                new ArrayList<>(vaccines.values())
        );
    }

    private Map<Long, String> toValueMap(
            Function<Set<Long>, List<IdValueProjection>> finder,
            Stream<Long> ids
    ) {
        Set<Long> distinctIds = ids.filter(Objects::nonNull).collect(Collectors.toSet());

        if (distinctIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> values = new HashMap<>();
        finder.apply(distinctIds).forEach(p -> values.put(p.getId(), p.getValue()));

        return values;
    }

    private String resolveLocation(PatientVaccinationReportProjection row) {
        if (Boolean.TRUE.equals(row.getIsExternalFacility())
                && row.getExternalFacilityName() != null
                && !row.getExternalFacilityName().isBlank()) {
            return row.getExternalFacilityName();
        }

        return row.getAdministeredLocation();
    }

    private String enumToNumber(List<String> values, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        int index = values.indexOf(value);

        return index >= 0 ? String.valueOf(index + 1) : reportCommonService.formatEnum(value);
    }

    private String formatRoa(String roa) {
        if (roa == null || roa.isBlank()) {
            return null;
        }

        return ROUTES_OF_ADMINISTRATION.getOrDefault(roa, reportCommonService.formatEnum(roa));
    }
}
