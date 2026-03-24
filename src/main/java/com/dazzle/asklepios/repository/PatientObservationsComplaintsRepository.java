package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientObservationsComplaints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientObservationsComplaintsRepository
        extends JpaRepository<PatientObservationsComplaints, Long> {

    Optional<PatientObservationsComplaints>
    findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(Long encounterId);
}
