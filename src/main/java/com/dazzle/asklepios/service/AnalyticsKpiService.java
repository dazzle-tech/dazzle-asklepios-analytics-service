package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Appointment;
import com.dazzle.asklepios.domain.enumeration.AppointmentStatus;
import com.dazzle.asklepios.domain.enumeration.KpiStatus;
import com.dazzle.asklepios.repository.AppointmentRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.service.dto.reports.AppointmentWaitTimeProjection;
import com.dazzle.asklepios.web.rest.vm.kpis.KpiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsKpiService {

    private static final String FACILITY_UTILIZATION_RATE =
            "FACILITY_UTILIZATION_RATE";

    private static final String DNA_NO_SHOW_RATE =
            "DNA_NO_SHOW_RATE";

    private static final String AVERAGE_WAIT_TIME_SCHEDULED =
            "AVERAGE_WAIT_TIME_SCHEDULED";
    private final PatientEncounterRepository patientEncounterRepository;

    @Value("${analytics.timezone:Asia/Gaza}")
    private String analyticsTimezone;

    /*
     * KPI targets from the KPI definition.
     */
    private static final BigDecimal FACILITY_UTILIZATION_TARGET = BigDecimal.valueOf(75);

    private static final BigDecimal NO_SHOW_TARGET = BigDecimal.valueOf(5);

    private static final BigDecimal AVERAGE_WAIT_TIME_TARGET = BigDecimal.valueOf(15);

    private static final String INCIDENT_RATE = "INCIDENT_RATE";

    private static final BigDecimal INCIDENT_RATE_TARGET = BigDecimal.valueOf(2);

    private static final String HAND_HYGIENE_COMPLIANCE = "HAND_HYGIENE_COMPLIANCE";

    private static final BigDecimal HAND_HYGIENE_TARGET = BigDecimal.valueOf(90);

    private final AppointmentRepository appointmentRepository;


    // =========================================================
    // 1. FACILITY UTILIZATION RATE
    // =========================================================

    /**
     * Facility Utilization Rate
     * <p>
     * Formula:
     * <p>
     * Booked Slots
     * ---------------- x 100
     * Available Slots
     * <p>
     * Target: >= 75%
     */
    public KpiResponse getFacilityUtilizationRate(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);

        Instant start = toStartOfDay(startDate);
        Instant end = toStartOfDay(endDate.plusDays(1));

        long availableSlots = appointmentRepository.countAppointmentByStatusInAndStartDatetimeIsGreaterThanEqualAndEndDatetimeIsLessThan(List.of(AppointmentStatus.NEW), start, end);

        long bookedSlots = appointmentRepository.countAppointmentByStatusInAndStartDatetimeIsGreaterThanEqualAndEndDatetimeIsLessThan(getBookedStatuses(), start, end);

        BigDecimal utilizationRate = calculatePercentage(bookedSlots, availableSlots);

        KpiStatus status;

        if (availableSlots == 0) {

            status = KpiStatus.NO_DATA;

        } else if (utilizationRate.compareTo(FACILITY_UTILIZATION_TARGET) >= 0) {

            status = KpiStatus.ACHIEVED;

        } else {

            status = KpiStatus.NOT_ACHIEVED;
        }

        KpiResponse response = new KpiResponse();

        response.setKpi(FACILITY_UTILIZATION_RATE);

        response.setLabel("Facility Utilization Rate");

        response.setValue(utilizationRate);

        response.setUnit("%");

        response.setTarget(FACILITY_UTILIZATION_TARGET);

        response.setTargetOperator(">=");

        response.setStatus(status.name());

        response.setStartDate(startDate);

        response.setEndDate(endDate);

        /*
         * numerator = booked slots
         * denominator = available slots
         */
        response.setNumerator(bookedSlots);

        response.setDenominator(availableSlots);

        return response;
    }


    // =========================================================
    // 2. DNA / NO-SHOW RATE
    // =========================================================

    /**
     * DNA / No-Show Rate
     * <p>
     * Formula:
     * <p>
     * No-Show Appointments
     * -------------------- x 100
     * Booked Appointments
     * <p>
     * Target: <= 5%
     */
    public KpiResponse getNoShowRate(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);

        Instant start = toStartOfDay(startDate);
        Instant end = toStartOfDay(endDate.plusDays(1));

        /*
         * Booked appointments.
         *
         * NO_SHOW is included because it was originally booked.
         *
         * CANCELLED is intentionally excluded.
         */
        long bookedAppointments = appointmentRepository.countAppointmentByStatusInAndStartDatetimeIsGreaterThanEqualAndEndDatetimeIsLessThan(getBookedStatuses(), start, end);

        /*
         * Appointments whose final status is NO_SHOW.
         */
        long noShowAppointments = appointmentRepository.countAppointmentByStatusInAndStartDatetimeIsGreaterThanEqualAndEndDatetimeIsLessThan(List.of(AppointmentStatus.NO_SHOW), start, end);

        BigDecimal noShowRate = calculatePercentage(noShowAppointments, bookedAppointments);

        KpiStatus status;

        if (bookedAppointments == 0) {

            status = KpiStatus.NO_DATA;

        } else if (noShowRate.compareTo(NO_SHOW_TARGET) <= 0) {

            status = KpiStatus.ACHIEVED;

        } else {

            status = KpiStatus.NOT_ACHIEVED;
        }

        KpiResponse response = new KpiResponse();

        response.setKpi(DNA_NO_SHOW_RATE);

        response.setLabel("DNA / No-Show Rate");

        response.setValue(noShowRate);

        response.setUnit("%");

        response.setTarget(NO_SHOW_TARGET);

        response.setTargetOperator("<=");

        response.setStatus(status.name());

        response.setStartDate(startDate);

        response.setEndDate(endDate);

        /*
         * numerator = no-show appointments
         * denominator = booked appointments
         */
        response.setNumerator(noShowAppointments);

        response.setDenominator(bookedAppointments);

        return response;
    }


    // =========================================================
    // 3. AVERAGE WAIT TIME - SCHEDULED
    // =========================================================

    /**
     * Average Wait Time (Scheduled)
     * <p>
     * Formula:
     * <p>
     * Encounter Start Time - Appointment Checked-In Time
     * <p>
     * Example:
     * <p>
     * Checked in:      09:10
     * Encounter start: 09:25
     * <p>
     * Wait time = 15 minutes
     * <p>
     * Target: <= 15 minutes.
     */
    public KpiResponse getAverageWaitTime(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);

        Instant start = toStartOfDay(startDate);
        Instant end = toStartOfDay(endDate.plusDays(1));

        /*
         * Get appointments that have:
         *
         * 1. checkedInAt
         * 2. encounter start time
         */
        List<AppointmentWaitTimeProjection> appointments = appointmentRepository.findAppointmentsForWaitTime(start, end);

        if (appointments == null || appointments.isEmpty()) {

            return buildNoDataWaitTimeResponse(startDate, endDate);
        }

        long totalWaitSeconds = 0;

        long validAppointmentCount = 0;

        for (AppointmentWaitTimeProjection appointment : appointments) {

            if (appointment == null) {
                continue;
            }

            Instant checkedInAt = appointment.getCheckedInAt();

            Instant encounterStart = appointment.getEncounterStartDate();

            /*
             * We cannot calculate the wait time if either
             * timestamp is missing.
             */
            if (checkedInAt == null || encounterStart == null) {
                continue;
            }

            /*
             * Wait Time =
             *
             * Encounter Start
             *       -
             * Appointment Checked In
             */
            Duration waitDuration = Duration.between(checkedInAt, encounterStart);

            long waitSeconds = waitDuration.getSeconds();

            /*
             * If encounter somehow started before check-in,
             * don't allow a negative wait time.
             */
            waitSeconds = Math.max(0, waitSeconds);

            totalWaitSeconds += waitSeconds;

            validAppointmentCount++;
        }

        /*
         * Appointments exist, but none had valid timestamps.
         */
        if (validAppointmentCount == 0) {

            return buildNoDataWaitTimeResponse(startDate, endDate);
        }

        /*
         * Calculate average wait time in minutes.
         */
        BigDecimal averageWaitMinutes =
                BigDecimal.valueOf(totalWaitSeconds)
                        .divide(BigDecimal.valueOf(validAppointmentCount * 60), 2, RoundingMode.HALF_UP);

        /*
         * Target:
         *
         * <= 15 minutes = ACHIEVED
         */
        KpiStatus status;

        if (averageWaitMinutes.compareTo(AVERAGE_WAIT_TIME_TARGET) <= 0) {

            status = KpiStatus.ACHIEVED;

        } else {

            status = KpiStatus.NOT_ACHIEVED;
        }

        KpiResponse response = new KpiResponse();

        response.setKpi(AVERAGE_WAIT_TIME_SCHEDULED);

        response.setLabel("Average Wait Time (Scheduled)");

        response.setValue(averageWaitMinutes);

        response.setUnit("min");

        response.setTarget(AVERAGE_WAIT_TIME_TARGET);

        response.setTargetOperator("<=");

        response.setStatus(status.name());

        response.setStartDate(startDate);

        response.setEndDate(endDate);

        response.setNumerator(validAppointmentCount);

        response.setDenominator(validAppointmentCount);

        return response;
    }

    // =========================================================
    // 4. TOTAL DAILY FOOTFALL
    // =========================================================

    @Transactional(readOnly = true)
    public KpiResponse getTotalDailyFootfall(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);

        Long totalDailyFootfall = patientEncounterRepository.countDistinctPatientsForDay(startDate, endDate);

        long value = totalDailyFootfall != null
                ? totalDailyFootfall
                : 0L;

        KpiStatus status;

        if (value == 0) {

            status = KpiStatus.NO_DATA;

        } else {
            status = KpiStatus.ACHIEVED;
        }

        KpiResponse response = new KpiResponse();

        response.setKpi("TOTAL_DAILY_FOOTFALL");

        response.setLabel("Total Daily Footfall");

        response.setValue(BigDecimal.valueOf(value));

        response.setUnit("#");

        /*
         * Target is currently TBD according to the KPI definition.
         */
        response.setTarget(null);

        response.setTargetOperator(null);

        response.setStatus(status.name());

        response.setStartDate(startDate);

        response.setEndDate(endDate);

        /*
         * numerator = null (not applicable)
         * denominator = null (not applicable)
         *
         * There is currently no ratio calculation for this KPI.
         */
        response.setNumerator(null);

        response.setDenominator(null);

        return response;
    }

//    // =========================================================
//    // 5. INCIDENT RATE
//    // =========================================================
//
//    /**
//     * Incident Rate
//     * <p>
//     * Formula:
//     * <p>
//     * Reported Patient Safety Incidents
//     * --------------------------------- x 1,000
//     * Total Patient Encounters
//     * <p>
//     * Target: < 2.0
//     */
//    public KpiResponse getIncidentRate(LocalDate startDate, LocalDate endDate) {
//
//        validateDates(startDate, endDate);
//
//        /*
//         * Number of reported patient safety incidents
//         * during the requested period.
//         */
//        long reportedIncidents =
//                incidentRepository.countReportedIncidents(startDate, endDate);
//
//        /*
//         * Total patient encounters during the
//         * requested period.
//         */
//        Long totalEncounters = patientEncounterRepository.countEncountersForPeriod(startDate, endDate);
//
//        long encounterCount = totalEncounters != null ? totalEncounters : 0L;
//
//        BigDecimal incidentRate;
//
//        if (encounterCount == 0) {
//
//            incidentRate = BigDecimal.ZERO;
//
//        } else {
//
//            incidentRate = BigDecimal.valueOf(reportedIncidents)
//                    .multiply(BigDecimal.valueOf(1000))
//                    .divide(
//                            BigDecimal.valueOf(encounterCount),
//                            2,
//                            RoundingMode.HALF_UP
//                    );
//        }
//
//        KpiStatus status;
//
//        if (encounterCount == 0) {
//
//            status = KpiStatus.NO_DATA;
//
//        } else if (incidentRate.compareTo(INCIDENT_RATE_TARGET) < 0) {
//
//            status = KpiStatus.ACHIEVED;
//
//        } else {
//
//            status = KpiStatus.NOT_ACHIEVED;
//        }
//
//        KpiResponse response = new KpiResponse();
//
//        response.setKpi(INCIDENT_RATE);
//
//        response.setLabel("Incident Rate");
//
//        response.setValue(incidentRate);
//
//        response.setUnit("per 1,000 encounters");
//
//        response.setTarget(INCIDENT_RATE_TARGET);
//
//        response.setTargetOperator("<");
//
//        response.setStatus(status.name());
//
//        response.setStartDate(startDate);
//
//        response.setEndDate(endDate);
//
//        /*
//         * numerator = reported incidents
//         * denominator = total encounters
//         */
//        response.setNumerator(reportedIncidents);
//
//        response.setDenominator(encounterCount);
//
//        return response;
//    }
//
//    // =========================================================
//// 6. HAND HYGIENE COMPLIANCE
//// =========================================================
//
//    /**
//     * Hand Hygiene Compliance
//     * <p>
//     * Formula:
//     * <p>
//     * Compliant Observed Events
//     * ------------------------- x 100
//     * Total Observed Events
//     * <p>
//     * Target: >= 90%
//     */
//    public KpiResponse getHandHygieneCompliance(LocalDate startDate, LocalDate endDate) {
//
//        validateDates(startDate, endDate);
//
//        /*
//         * Number of compliant hand hygiene observations.
//         */
//        long compliantEvents = handHygieneAuditRepository
//                .countCompliantObservations(
//                        startDate,
//                        endDate
//                );
//
//        /*
//         * Total number of observed hand hygiene events.
//         */
//        long totalObservedEvents = handHygieneAuditRepository
//                .countObservedEvents(
//                        startDate,
//                        endDate
//                );
//
//        BigDecimal complianceRate = calculatePercentage(compliantEvents, totalObservedEvents);
//
//        KpiStatus status;
//
//        if (totalObservedEvents == 0) {
//
//            status = KpiStatus.NO_DATA;
//
//        } else if (complianceRate.compareTo(HAND_HYGIENE_TARGET) >= 0) {
//
//            status = KpiStatus.ACHIEVED;
//
//        } else {
//
//            status = KpiStatus.NOT_ACHIEVED;
//        }
//
//        KpiResponse response = new KpiResponse();
//
//        response.setKpi(HAND_HYGIENE_COMPLIANCE);
//
//        response.setLabel("Hand Hygiene Compliance");
//
//        response.setValue(complianceRate);
//
//        response.setUnit("%");
//
//        response.setTarget(
//                HAND_HYGIENE_TARGET
//        );
//
//        response.setTargetOperator(">=");
//
//        response.setStatus(status.name());
//
//        response.setStartDate(startDate);
//
//        response.setEndDate(endDate);
//
//        /*
//         * numerator = compliant observations
//         * denominator = total observations
//         */
//        response.setNumerator(compliantEvents);
//
//        response.setDenominator(totalObservedEvents);
//
//        return response;
//    }

    // =========================================================
    // STATUS HELPERS
    // =========================================================

    /**
     * Appointment statuses that represent appointments which
     * entered the booking process.
     * <p>
     * NO_SHOW must be included because a no-show was originally
     * booked.
     * <p>
     * CANCELLED is excluded.
     */
    private List<AppointmentStatus> getBookedStatuses() {

        return List.of(
                AppointmentStatus.BOOKED,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.CHECKED_IN,
                AppointmentStatus.IN_SERVICE,
                AppointmentStatus.COMPLETED,
                AppointmentStatus.NO_SHOW
        );
    }


    // =========================================================
    // CALCULATION HELPERS
    // =========================================================

    /**
     * Calculates:
     * <p>
     * numerator / denominator * 100
     */
    private BigDecimal calculatePercentage(long numerator, long denominator) {

        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(denominator),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // =========================================================
    // DATE HELPERS
    // =========================================================

    /**
     * Converts the beginning of a LocalDate into Instant using
     * the configured analytics timezone.
     */
    private Instant toStartOfDay(LocalDate date) {
        return date
                .atStartOfDay(ZoneId.of(analyticsTimezone))
                .toInstant();
    }


    // =========================================================
    // RESPONSE HELPERS
    // =========================================================

    private KpiResponse buildNoDataWaitTimeResponse(
            LocalDate startDate,
            LocalDate endDate
    ) {

        KpiResponse response = new KpiResponse();

        response.setKpi(
                AVERAGE_WAIT_TIME_SCHEDULED
        );

        response.setLabel(
                "Average Wait Time (Scheduled)"
        );

        response.setValue(
                BigDecimal.ZERO
        );

        response.setUnit("min");

        response.setTarget(
                AVERAGE_WAIT_TIME_TARGET
        );

        response.setTargetOperator("<=");

        response.setStatus(
                KpiStatus.NO_DATA.name()
        );

        response.setStartDate(startDate);

        response.setEndDate(endDate);

        response.setNumerator(0L);

        response.setDenominator(0L);

        return response;
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null) {

            throw new IllegalArgumentException(
                    "startDate is required"
            );
        }

        if (endDate == null) {

            throw new IllegalArgumentException(
                    "endDate is required"
            );
        }

        if (endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "endDate must be greater than or equal to startDate"
            );
        }
    }
}