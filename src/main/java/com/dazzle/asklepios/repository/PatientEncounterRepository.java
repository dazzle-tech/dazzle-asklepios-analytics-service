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
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientEncounterRepository extends JpaRepository<PatientEncounter, Long> , JpaSpecificationExecutor<PatientEncounter> {
    @Query("""
    select new com.dazzle.asklepios.service.dto.PatientEncounterReportDTO(
        d.name,
        pe.status
    )
    from PatientEncounter pe
    join pe.department d
    where
        (:departmentId is null or d.id = :departmentId)
    and
        (:status is null or pe.status = :status)
    order by pe.createdDate desc
    """)
    List<PatientEncounterReportDTO> getPatientEncounterReport(
            @Param("departmentId") Long departmentId,
            @Param("status") String status
    );
}
