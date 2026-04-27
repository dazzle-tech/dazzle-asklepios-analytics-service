package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<PatientDiagnosis, Long> {
    List<PatientDiagnosis> findAllByEncounterId(Long encounterId);
}