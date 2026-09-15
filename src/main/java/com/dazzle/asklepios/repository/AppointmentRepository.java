package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Appointment;
import com.dazzle.asklepios.domain.enumeration.AppointmentStatus;
import com.dazzle.asklepios.domain.enumeration.TemplateType;
import com.dazzle.asklepios.service.dto.reports.AppointmentWaitTimeProjection;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface AppointmentRepository  extends JpaRepository<Appointment, Long> {

    long countAppointmentByStatusInAndStartDatetimeIsGreaterThanEqualAndEndDatetimeIsLessThan(List<AppointmentStatus> status, Instant start, Instant end);

    @Query("""
    SELECT
        a.checkedInAt AS checkedInAt,
        e.startedDate AS encounterStartDate
    FROM Appointment a
    JOIN PatientEncounter e
        ON e.appointment.id = a.id
    WHERE a.startDatetime >= :start
      AND a.startDatetime < :end
      AND a.checkedInAt IS NOT NULL
      AND e.startedDate IS NOT NULL
    """)
    List<AppointmentWaitTimeProjection> findAppointmentsForWaitTime(
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
    SELECT COUNT(a)
    FROM Appointment a
    WHERE a.status IN :statuses
      AND a.departmentId = :departmentId
      AND a.startDatetime >= :start
      AND a.endDatetime < :end
      AND a.patient IS NOT NULL
""")
    long countAppointmentByStatusInAndDepartmentIdAndStartDatetimeGreaterThanEqualAndEndDatetimeLessThan(
            @Param("statuses") List<AppointmentStatus> statuses,
            @Param("departmentId") Long departmentId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
    SELECT COUNT(a)
    FROM Appointment a
    WHERE a.status = :status
      AND a.departmentId = :departmentId
      AND a.startDatetime >= :start
      AND a.endDatetime < :end
      AND a.patient IS NOT NULL
""")
    long countAppointmentByStatusAndDepartmentIdAndStartDatetimeGreaterThanEqualAndEndDatetimeLessThan(
            @Param("status") AppointmentStatus status,
            @Param("departmentId") Long departmentId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
    SELECT COUNT(a)
    FROM Appointment a
    WHERE a.resourceType = :resourceType
      AND a.resourceId IN :resourceIds
      AND a.status IN :statuses
      AND a.startDatetime >= :start
      AND a.startDatetime < :end
    """)
    long countByResourceTypeAndResourceIdInAndStatusInAndStartDatetimeRange(
            @Param("resourceType") TemplateType resourceType,
            @Param("resourceIds") List<Long> resourceIds,
            @Param("statuses") List<AppointmentStatus> statuses,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
    SELECT COUNT(a)
    FROM Appointment a
    WHERE a.resourceType = :resourceType
      AND a.resourceId IN :resourceIds
      AND a.status = :status
      AND a.startDatetime >= :start
      AND a.startDatetime < :end
    """)
    long countByResourceTypeAndResourceIdInAndStatusAndStartDatetimeRange(
            @Param("resourceType") TemplateType resourceType,
            @Param("resourceIds") List<Long> resourceIds,
            @Param("status") AppointmentStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

}
