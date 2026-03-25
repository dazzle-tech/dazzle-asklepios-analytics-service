package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, Long> {
    PatientInsurance findTopByPatient_IdOrderByIdDesc(Long patientId);
}