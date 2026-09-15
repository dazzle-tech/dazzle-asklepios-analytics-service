package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientProblem;
import com.dazzle.asklepios.domain.enumeration.PatientHistoryStatus;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PatientProblemRepository extends JpaRepository<PatientProblem, Long> {
    @Query(value = """
    SELECT COUNT(DISTINCT pp.patient_id)
    FROM patient_problems pp
    JOIN patients p
        ON p.id = pp.patient_id
    JOIN patient_encounters e
        ON e.patient_id = p.id
    WHERE pp.active = true
      AND e.department_id = :departmentId
      AND e.encounter_date >= :startDate
      AND e.encounter_date < :endDate
        AND pp.status =( SELECT key FROM ap_lov_values WHERE value_code = 'DIAG_STATUS_CHRON' LIMIT 1)
""", nativeQuery = true)
    long countChronicDiseasePatientsByDepartment(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    @Query(value = """
    SELECT COUNT(DISTINCT pp.patient_id)
    FROM patient_problems pp
    JOIN patients p
        ON p.id = pp.patient_id
    JOIN patient_encounters e
        ON e.patient_id = p.id
    WHERE pp.active = true
      AND e.department_id = :departmentId
      AND e.encounter_date >= :startDate
      AND e.encounter_date < :endDate
      AND (
          pp.condition LIKE '%TYPE_1_DIABETIC%'
          OR pp.condition LIKE '%TYPE_2_DIABETIC%'
      )
    """, nativeQuery = true)
    long countDiabeticPatientsByDepartment(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    @Query(value = """
            SELECT COUNT(DISTINCT pp.patient_id)
            FROM patient_problems pp
            JOIN patients p
                ON p.id = pp.patient_id
            JOIN patient_encounters e
                ON e.patient_id = p.id
            JOIN diagnostic_orders do
                ON do.patient_id = p.id
            JOIN diagnostic_order_tests dot
                ON dot.diagnostic_order_id = do.id
            JOIN diagnostic_tests dt
                ON dt.id = dot.test_id
            JOIN diagnostic_order_test_results dotr
                ON dotr.order_test_id = dot.id
            WHERE pp.active = true
              AND e.department_id = :departmentId
              AND e.encounter_date >= :startDate
              AND e.encounter_date < :endDate
              AND (
                  pp.condition LIKE '%TYPE_1_DIABETIC%'
                  OR pp.condition LIKE '%TYPE_2_DIABETIC%'
              )
              AND dotr.created_date >= :hba1cStartDate
              AND dotr.created_date < :endDate
              AND dt.name like '%HBA1C%'   """, nativeQuery = true)
    long countDiabeticPatientsWithHba1cWithinSixMonthsByDepartment(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("hba1cStartDate") LocalDate hba1cStartDate,
            @Param("departmentId") Long departmentId
    );
}
