package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientProcedureMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientProcedureMedicationRepository extends JpaRepository<PatientProcedureMedication, Long> {

    List<PatientProcedureMedication> findByPatientProcedureIdOrderByIdAsc(Long patientProcedureId);
}
