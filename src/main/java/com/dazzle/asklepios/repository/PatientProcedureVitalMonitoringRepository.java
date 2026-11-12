package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientProcedureVitalMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientProcedureVitalMonitoringRepository extends JpaRepository<PatientProcedureVitalMonitoring, Long> {

    List<PatientProcedureVitalMonitoring> findByPatientProcedureIdOrderByRecordedAtAscIdAsc(Long patientProcedureId);
}
