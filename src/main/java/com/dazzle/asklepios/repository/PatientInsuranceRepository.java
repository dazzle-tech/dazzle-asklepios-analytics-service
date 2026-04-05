package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, Long> {
    Optional<PatientInsurance> findFirstByPatientIdAndIsPrimaryTrue(Long patientId);

    PatientInsurance findTopByPatient_IdOrderByIdDesc(Long patientId);
}