package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientProcedureServiceEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientProcedureServiceEquipmentRepository extends JpaRepository<PatientProcedureServiceEquipment, Long> {

    List<PatientProcedureServiceEquipment> findByPatientProcedureIdOrderByIdAsc(Long patientProcedureId);
}
