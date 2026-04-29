package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.EncounterVaccination;
import com.dazzle.asklepios.domain.enumeration.EncounterVaccinationStatus;
import io.micrometer.core.instrument.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncounterVaccinationRepository
        extends JpaRepository<EncounterVaccination, Long> {

    Page<EncounterVaccination> findByPatient_Id(
            Long patientId,
            Pageable pageable
    );

    Page<EncounterVaccination> findByEncounterId(
            Long encounterId,
            Pageable pageable
    );


    Page<EncounterVaccination> findByPatient_IdAndStatusNot(
            Long patientId,
            EncounterVaccinationStatus status,
            Pageable pageable
    );

    Page<EncounterVaccination> findByEncounterIdAndStatusNot(
            Long encounterId,
            EncounterVaccinationStatus status,
            Pageable pageable
    );


    Page<EncounterVaccination> findByPatient_IdAndVaccineId(
            Long patientId,
            Long vaccineId,
            Pageable pageable
    );

    Page<EncounterVaccination> findByPatient_IdAndVaccineIdAndStatusNot(
            Long patientId,
            Long vaccineId,
            EncounterVaccinationStatus status,
            Pageable pageable
    );


    List<EncounterVaccination> findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
            Long encounterId,
            EncounterVaccinationStatus status
    );
}
