package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientProcedureComplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientProcedureComplicationRepository extends JpaRepository<PatientProcedureComplication, Long> {

    List<PatientProcedureComplication> findByPatientProcedureIdOrderByIdAsc(Long patientProcedureId);
}
