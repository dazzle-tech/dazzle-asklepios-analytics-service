package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.enumeration.EncounterStatus;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.service.dto.PatientEncounterReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientEncounterRepository extends JpaRepository<PatientEncounter, Long> , JpaSpecificationExecutor<PatientEncounter> {

    @Query("""
    SELECT COUNT(DISTINCT e.patient.id)
    FROM PatientEncounter e
    WHERE e.encounterDate >= :start
      AND e.encounterDate < :end
""")
    Long countDistinctPatientsForDay(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
    SELECT pe
    FROM PatientEncounter pe
    JOIN FETCH pe.patient p
    LEFT JOIN FETCH pe.practitioner pr
    LEFT JOIN FETCH pe.department d
    WHERE pe.encounterDate >= :visitDate
      AND pe.encounterDate < :nextDate
    ORDER BY pe.encounterDate ASC
    """)
    List<PatientEncounter> findDailyPatientVisits(
            @Param("visitDate") LocalDate visitDate,
            @Param("nextDate") LocalDate nextDate
    );

    @Query("""
    SELECT
        e.id AS encounterId,
        e.createdDate AS startTime,
        e.startedDate AS endTime
    FROM PatientEncounter e
  
    WHERE e.encounterDate >= :startDate
      AND e.encounterDate < :endDate
      AND e.startedDate IS NOT NULL
      AND e.encounterType = :encounterType
""")
    List<KpiDurationProjection> findDoorToDoctorTimes(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("encounterType") EncounterType encounterType
    );

    @Query("""
    SELECT
        e.id AS encounterId,
        e.createdDate AS startTime,
        e.dischargeAt AS endTime
    FROM PatientEncounter e 
    WHERE e.encounterDate >= :startDate
      AND e.encounterDate < :endDate
      AND e.createdDate IS NOT NULL
      AND e.dischargeAt IS NOT NULL
      AND e.encounterType = :encounterType
""")
    List<KpiDurationProjection> findUccLengthOfStay(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("encounterType") EncounterType encounterType
    );

    @Query("""
    SELECT
        e.id AS encounterId,
        e.createdDate AS startTime,
        et.completedDate AS endTime
    FROM PatientEncounter e
   left outer join EmergencyTriage et ON e.id = et.encounter.id
    WHERE e.encounterDate >= :startDate
      AND e.encounterDate < :endDate
      AND e.createdDate IS NOT NULL
      AND et.completedDate IS NOT NULL
      AND e.encounterType = :encounterType
""")
    List<KpiDurationProjection> findTriageCompletionTimes(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("encounterType") EncounterType encounterType
    );


    @Query("""
    SELECT COUNT(e)
    FROM PatientEncounter e
    WHERE e.encounterDate >= :startDate
      AND e.encounterDate < :endDate
      AND e.encounterType = :encounterType
      AND e.status IN :statuses
""")
    long countLeftWithoutBeingSeen(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("encounterType") EncounterType encounterType,
            @Param("statuses") Collection<String> statuses
    );

    @Query("""
    SELECT COUNT(e)
    FROM PatientEncounter e
    WHERE e.encounterDate >= :startDate
      AND e.encounterDate < :endDate
      AND e.encounterType = :encounterType
""")
    long countEncountersByType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("encounterType") EncounterType encounterType
    );


    @Query("""
    SELECT COUNT(e)
    FROM PatientEncounter e
    JOIN EncounterDischargeLog dl
        ON dl.encounter.id = e.id
    WHERE e.encounterDate >= :startDate
      AND e.encounterDate < :endDate
      AND e.encounterType = :encounterType
      AND dl.dischargedAt IS NOT NULL
""")
    long countDischargesByEncounterType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("encounterType") EncounterType encounterType
    );

    @Query(value = """
    SELECT COUNT(DISTINCT e2.patient_id)
    FROM patient_encounters e1
    JOIN encounter_discharge_log dl
        ON dl.encounter_id = e1.id
    JOIN patient_encounters e2
        ON e2.patient_id = e1.patient_id
       AND e2.id <> e1.id
    WHERE e1.encounter_type = :encounterType
      AND e2.encounter_type = :encounterType
      AND dl.discharged_at IS NOT NULL

      -- Previous UCC discharge must be in the KPI period
      AND dl.discharged_at >= :startDate
      AND dl.discharged_at < :endDate

      -- New UCC attendance must occur after discharge
      AND e2.encounter_date > dl.discharged_at

      -- And within 72 hours
      AND e2.encounter_date <= dl.discharged_at + INTERVAL '72 hours'
""", nativeQuery = true)
    long countUnplannedReattendance(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("encounterType") String encounterType
    );

    @Query("""
    SELECT
        e.id AS encounterId,
        e.startedDate AS startTime,
        e.dischargeAt AS endTime
    FROM PatientEncounter e
    WHERE e.encounterDate >= :startDate
      AND e.encounterDate < :endDate
      AND e.department.id = :departmentId
      AND e.startedDate IS NOT NULL
      AND e.dischargeAt IS NOT NULL
""")
    List<KpiDurationProjection> findConsultationDurations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );
}
