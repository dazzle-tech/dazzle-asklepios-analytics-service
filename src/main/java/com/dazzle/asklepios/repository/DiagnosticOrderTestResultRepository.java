package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.enumeration.DiagnosticStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

public interface DiagnosticOrderTestResultRepository
        extends JpaRepository<DiagnosticOrderTestResult, Long>,
        JpaSpecificationExecutor<DiagnosticOrderTestResult> {

    List<DiagnosticOrderTestResult> findByOrderTestIdIn(List<Long> orderTestIds);
    List<DiagnosticOrderTestResult> findByOrderTestIdInOrderByCreatedDateDesc(List<Long> orderTestIds);

    List<DiagnosticOrderTestResult> findByOrderTestIdInAndApprovedDateBetweenAndProcessingStatus(
            List<Long> orderTestIds,
            Instant from,
            Instant to,
            DiagnosticStatus status
    );

    List<DiagnosticOrderTestResult> findByOrderTestIdInAndApprovedDateBeforeAndProcessingStatusOrderByApprovedDateDesc(
            List<Long> orderTestIds,
            Instant before,
            DiagnosticStatus status
    );
}

