package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.EncounterVaccination;
import com.dazzle.asklepios.domain.enumeration.EncounterVaccinationStatus;
import io.micrometer.core.instrument.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EncounterVaccinationRepository
        extends JpaRepository<EncounterVaccination, Long> {

    List<EncounterVaccination> findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
            Long encounterId,
            EncounterVaccinationStatus status
    );

    @Query(value = """
        SELECT
            v.id                       AS vaccineId,
            v.name                     AS vaccineName,
            v.atc_code                 AS atcCode,
            v.type                     AS vaccineType,
            v.number_of_doses          AS numberOfDoses,
            v.roa                      AS roa,
            v.site_of_administration   AS siteOfAdministration,
            ev.vaccine_brand_id        AS vaccineBrandId,
            ev.vaccine_dose_id         AS vaccineDoseId,
            ev.date_administered       AS dateAdministered,
            ev.administered_location   AS administeredLocation,
            ev.is_external_facility    AS isExternalFacility,
            ev.external_facility_name  AS externalFacilityName
        FROM encounter_vaccination ev
        JOIN vaccine v ON v.id = ev.vaccine_id
        WHERE ev.patient_id = :patientId
          AND ev.status <> :excludedStatus
        ORDER BY
            v.name ASC,
            v.id ASC,
            ev.date_administered ASC
        """, nativeQuery = true)
    List<PatientVaccinationReportProjection> findPatientVaccinationReportRows(
            @Param("patientId") Long patientId,
            @Param("excludedStatus") String excludedStatus
    );

    @Query(value = """
        SELECT
            vb.id    AS id,
            vb.name  AS value
        FROM vaccine_brands vb
        WHERE vb.id IN (:ids)
        """, nativeQuery = true)
    List<IdValueProjection> findVaccineBrandNamesByIds(@Param("ids") Collection<Long> ids);

    @Query(value = """
        SELECT
            vd.id           AS id,
            vd.dose_number  AS value
        FROM vaccine_doses vd
        WHERE vd.id IN (:ids)
        """, nativeQuery = true)
    List<IdValueProjection> findVaccineDoseNumbersByIds(@Param("ids") Collection<Long> ids);
}
