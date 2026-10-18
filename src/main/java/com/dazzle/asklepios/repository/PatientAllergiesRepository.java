package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientAllergies;
import io.micrometer.core.instrument.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PatientAllergiesRepository extends JpaRepository<PatientAllergies, Long>, JpaSpecificationExecutor<PatientAllergies> {

    List<PatientAllergies> findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
            Long encounterId,
            String status
    );
    @EntityGraph(attributePaths = {"allergen","medicationClass"})
    List<PatientAllergies> findByPatientIdAndStatusNotOrderByCreatedDateAsc(
            Long patientId,
            String status
    );


    List<PatientAllergies> findByPatientIdOrderByCreatedDateAsc(Long patientId);

    List<PatientAllergies> findByEncounterIdAndStatusOrderByCreatedDateAsc(
            Long encounterId,
            String status
    );

}
