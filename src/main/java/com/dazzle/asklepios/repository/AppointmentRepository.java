package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Appointment;
import com.dazzle.asklepios.domain.enumeration.AppointmentStatus;
import com.dazzle.asklepios.service.dto.reports.AppointmentWaitTimeProjection;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDate;
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

}
