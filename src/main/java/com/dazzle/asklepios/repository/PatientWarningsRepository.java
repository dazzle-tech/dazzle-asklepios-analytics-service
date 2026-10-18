package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientWarnings;
import io.micrometer.core.instrument.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PatientWarningsRepository extends JpaRepository<PatientWarnings, Long>, JpaSpecificationExecutor<PatientWarnings> {

    List<PatientWarnings> findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
            Long encounterId,
            String status
    );
}