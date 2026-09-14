package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.enumeration.DiagnosticStatus;
import com.dazzle.asklepios.domain.enumeration.Severity;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.domain.enumeration.diagnostictest.TestResultMarker;
import com.dazzle.asklepios.service.dto.reports.DiagnosticResultReportDTO;
import com.dazzle.asklepios.service.dto.reports.criticalResult.CriticalResultCommunicationDTO;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface DiagnosticOrderTestResultRepository extends JpaRepository<DiagnosticOrderTestResult, Long>, JpaSpecificationExecutor<DiagnosticOrderTestResult> {

    List<DiagnosticOrderTestResult> findByOrderTestIdInOrderByCreatedDateDesc(List<Long> orderTestIds);

    List<DiagnosticOrderTestResult> findByOrderTestIdIn(List<Long> orderTestIds);


    @Query(value = """
    SELECT
        p.first_name AS firstName,
        p.last_name AS lastName,
        p.medical_record_number AS medicalRecordNumber,
        pe.encounter_number AS encounterNumber,
        pe.created_date AS visitDate,

        department.name AS departmentName,

        pr.first_name AS practitionerFirstName,
        pr.last_name AS practitionerLastName,

        dt.name AS testName,
        dtp.name AS profileName,

        dot.created_date AS testOrderedDate,
        dot.order_type AS testType,

        received_department.name AS receivedDepartmentName,

        dot.status AS testStatus,
        d.status AS orderStatus,

        result.result_value_number AS resultValueNumber,
        result.result_value_text AS resultValueText,
        result.normal_range_value AS resultNormalRangeValue,
        dtp.result_type AS resultType,
        dtp.result_unit AS resultUnit,
        result.created_date AS resultValueDate,

        result.approved_by AS resultApprovedBy,
        result.approved_date AS resultApprovedDate,

        result.marker AS resultStatus

    FROM diagnostic_order_tests dot

    INNER JOIN diagnostic_orders d
        ON d.id = dot.order_id

    INNER JOIN patient_encounters pe
        ON pe.id = d.encounter_id

    INNER JOIN patients p
        ON p.id = d.patient_id

    INNER JOIN practitioner pr
        ON pr.id = pe.practitioner_id

    INNER JOIN department
        ON department.id = pe.department_id

    INNER JOIN department received_department
        ON received_department.id = dot.received_department_id

    INNER JOIN diagnostic_test dt
        ON dt.id = dot.test_id

    LEFT JOIN diagnostic_order_tests_result result
        ON result.order_test_id = dot.id

    LEFT JOIN diagnostic_test_profile dtp
        ON dtp.id = result.profile_test_id

    WHERE dot.order_type = :orderType

      AND pe.encounter_date >= :startDate

      AND pe.encounter_date < :endDate

    ORDER BY
        dot.created_date ASC,
        dot.order_type DESC
    """,
            nativeQuery = true)
    List<DiagnosticResultReportDTO> findDiagnosticResults(
            @Param("orderType") String orderType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

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
