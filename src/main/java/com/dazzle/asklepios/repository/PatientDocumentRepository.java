package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientDocumentRepository extends JpaRepository<PatientDocument, Long> {

    Page<PatientDocument> findByIsPrimaryTrueAndNumberContainingIgnoreCase(
            String numberPart,
            Pageable pageable
    );

    Page<PatientDocument> findByNumberContainingIgnoreCase(String numberPart, Pageable pageable);
    Optional<PatientDocument> findFirstByPatientIdAndIsPrimaryTrue(Long patientId);
    Optional<PatientDocument> findTopByPatientIdAndIsPrimaryTrueOrderByIdDesc(Long patientId);
}