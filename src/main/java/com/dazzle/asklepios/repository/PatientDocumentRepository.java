package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientDocumentRepository extends JpaRepository<PatientDocument, Long> {

    Optional<PatientDocument> findTopByPatientIdAndIsPrimaryTrueOrderByIdDesc(Long patientId);
}