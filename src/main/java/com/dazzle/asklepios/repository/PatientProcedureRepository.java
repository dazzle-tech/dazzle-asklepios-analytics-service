package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientProcedureRepository
        extends JpaRepository<PatientProcedure, Long> {

    List <PatientProcedure> findByEncounterIdAndStatusNotOrderByCreatedDateAsc(Long encounterId ,String status);

    List<PatientProcedure> findByPatient_IdOrderByCreatedDateAsc(Long patientId);
}
