package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientPrescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PatientPrescriptionRepository extends JpaRepository<PatientPrescription, Long> {
    List<PatientPrescription> findByEncounterIdOrderByCreatedDateAsc(Long encounterId);

}