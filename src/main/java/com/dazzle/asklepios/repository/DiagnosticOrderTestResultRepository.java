package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DiagnosticOrderTestResultRepository
        extends JpaRepository<DiagnosticOrderTestResult, Long>,
        JpaSpecificationExecutor<DiagnosticOrderTestResult> {


}
