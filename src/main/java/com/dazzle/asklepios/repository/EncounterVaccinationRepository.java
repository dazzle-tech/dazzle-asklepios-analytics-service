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

    List<EncounterVaccination> findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
            Long encounterId,
            EncounterVaccinationStatus status
    );
}
