package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.enumeration.Severity;
import com.dazzle.asklepios.domain.enumeration.diagnostictest.TestResultMarker;
import com.dazzle.asklepios.service.dto.reports.criticalResult.CriticalResultCommunicationDTO;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface DiagnosticOrderTestResultRepository
        extends JpaRepository<DiagnosticOrderTestResult, Long>,
        JpaSpecificationExecutor<DiagnosticOrderTestResult> {
    List<DiagnosticOrderTestResult> findByOrderTestIdInOrderByCreatedDateDesc(List<Long> orderTestIds);

//    @Query(value = """
//        SELECT
//            r.id AS resultId,
//            ot.order_type AS testType,
//            r.approved_date AS criticalAt,
//            r.approved_date AS communicatedAt
//        FROM diagnostic_order_test_result r
//        JOIN diagnostic_order_test ot
//            ON ot.id = r.diagnostic_order_test_id
//        LEFT JOIN diagnostic_order_test_report tr
//            ON tr.diagnostic_order_test_id = ot.id
//        WHERE r.approved_date >= :start
//          AND r.approved_date < :end
//          AND (
//                (
//                    ot.order_type = 'LABORATORY'
//                    AND r.result_marker IN ('CRITICAL_UPPER', 'CRITICAL_LOWER')
//                )
//                OR
//                (
//                    ot.order_type = 'RADIOLOGY'
//                    AND tr.severity IN ('SEVERE', 'CRITICAL')
//                )
//          )
//        """, nativeQuery = true)
//    List<CriticalResultCommunicationDTO> findCriticalResultCommunications(
//            @Param("start") Instant start,
//            @Param("end") Instant end
//    );


}
