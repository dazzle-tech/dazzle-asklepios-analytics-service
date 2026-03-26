package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientWarnings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientWarningRepository extends JpaRepository<PatientWarnings, Long> {
    List<PatientWarnings> findAllByPatientId(Long patientId);
}