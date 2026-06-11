package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticTestLaboratoryRepository extends JpaRepository<DiagnosticTestLaboratory, Long> {

    Optional<DiagnosticTestLaboratory> findByTest_Id(Long testId);
}