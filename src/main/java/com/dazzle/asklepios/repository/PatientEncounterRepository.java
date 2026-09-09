package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.enumeration.EncounterStatus;
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
}
