package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DiagnosticOrderTestReportRepository
        extends JpaRepository<DiagnosticOrderTestReport, Long>,
        JpaSpecificationExecutor<DiagnosticOrderTestReport> {

    List<DiagnosticOrderTestReport> findByOrderTestIdInOrderByCreatedDateDesc(List<Long> orderTestIds);

}
