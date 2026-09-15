package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.AppointmentStatus;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.KpiStatus;
import com.dazzle.asklepios.domain.enumeration.TemplateType;
import com.dazzle.asklepios.repository.AppointmentRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestResultRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.KpiDurationProjection;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientProblemRepository;
import com.dazzle.asklepios.service.dto.reports.AppointmentWaitTimeProjection;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.kpis.KpiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsKpiService.class);

    private static final String FACILITY_UTILIZATION_RATE = "FACILITY_UTILIZATION_RATE";
    private static final String FACILITY_UTILIZATION_RATE_LABEL = "Facility Utilization Rate";

    private static final String DNA_NO_SHOW_RATE = "DNA_NO_SHOW_RATE";
    private static final String DNA_NO_SHOW_RATE_LABEL = "DNA / No-Show Rate";

    private static final String AVERAGE_WAIT_TIME_SCHEDULED = "AVERAGE_WAIT_TIME_SCHEDULED";
    private static final String AVERAGE_WAIT_TIME_SCHEDULED_LABEL = "Average Wait Time (Scheduled)";

    private static final String MODALITY_UTILISATION_CT = "MODALITY_UTILISATION_CT";
    private static final String MODALITY_UTILISATION_CT_LABEL = "Modality Utilisation - CT";

    private static final String DOOR_TO_DOCTOR_TIME = "DOOR_TO_DOCTOR_TIME";
    private static final String DOOR_TO_DOCTOR_LABEL = "Door-to-Doctor Time";

    private static final String UCC_LENGTH_OF_STAY = "UCC_LENGTH_OF_STAY";
    private static final String UCC_LENGTH_OF_STAY_LABEL = "UCC Length of Stay";

    private static final String TRIAGE_COMPLETION_TIME = "TRIAGE_COMPLETION_TIME";
    private static final String TRIAGE_COMPLETION_LABEL = "Triage Completion Time";

    private static final String LEFT_WITHOUT_BEING_SEEN = "LEFT_WITHOUT_BEING_SEEN";
    private static final String LEFT_WITHOUT_BEING_SEEN_LABEL = "Left Without Being Seen";

    private static final String AVG_CONSULTATION_DURATION = "AVG_CONSULTATION_DURATION";
    private static final String AVG_CONSULTATION_DURATION_LABEL = "Avg Consultation Duration";

    private static final String CRITICAL_RESULT_NOTIFICATION = "CRITICAL_RESULT_NOTIFICATION";
    private static final String CRITICAL_RESULT_NOTIFICATION_LABEL = "Critical Result Notification";

    private static final String DIABETIC_HBA1C_MONITORING = "DIABETIC_HBA1C_MONITORING";
    private static final String DIABETIC_HBA1C_MONITORING_LABEL = "Diabetic HbA1c Monitoring";


    private final PatientEncounterRepository patientEncounterRepository;
    private final DiagnosticOrderTestResultRepository diagnosticOrderTestResultRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final PatientProblemRepository patientProblemRepository;

    @Value("${analytics.timezone:Asia/Gaza}")
    private String analyticsTimezone;

    /*
     * KPI targets from the KPI definition.
     */
    private static final BigDecimal FACILITY_UTILIZATION_TARGET = BigDecimal.valueOf(75);
    private static final BigDecimal NO_SHOW_TARGET = BigDecimal.valueOf(5);
    private static final BigDecimal UCC_LENGTH_OF_STAY_TARGET = BigDecimal.valueOf(120);
    private static final BigDecimal DOOR_TO_DOCTOR_TARGET = BigDecimal.valueOf(15);
    private static final BigDecimal AVERAGE_WAIT_TIME_TARGET = BigDecimal.valueOf(15);
    private static final BigDecimal TRIAGE_COMPLETION_TARGET = BigDecimal.valueOf(5);
    private static final BigDecimal LEFT_WITHOUT_BEING_SEEN_TARGET = BigDecimal.valueOf(3);
    private static final BigDecimal CRITICAL_RESULT_NOTIFICATION_TARGET = BigDecimal.valueOf(95);
    private static final BigDecimal AVG_CONSULTATION_DURATION_MIN = BigDecimal.valueOf(12);
    private static final BigDecimal AVG_CONSULTATION_DURATION_MAX = BigDecimal.valueOf(18);
    private static final BigDecimal MODALITY_UTILISATION_CT_TARGET = BigDecimal.valueOf(60);
    private static final BigDecimal DIABETIC_HBA1C_TARGET = BigDecimal.valueOf(85);


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

        return buildResponse(
                FACILITY_UTILIZATION_RATE,
                FACILITY_UTILIZATION_RATE_LABEL,
                utilizationRate,
                "%",
                FACILITY_UTILIZATION_TARGET,
                ">=",
                status,
                startDate,
                endDate,
                bookedSlots,
                availableSlots
        );
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

        return buildResponse(
                DNA_NO_SHOW_RATE,
                DNA_NO_SHOW_RATE_LABEL,
                noShowRate,
                "%",
                NO_SHOW_TARGET,
                "<=",
                status,
                startDate,
                endDate,
                noShowAppointments,
                bookedAppointments
        );
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

        return buildResponse(
                AVERAGE_WAIT_TIME_SCHEDULED,
                AVERAGE_WAIT_TIME_SCHEDULED_LABEL,
                averageWaitMinutes,
                "min",
                AVERAGE_WAIT_TIME_TARGET,
                "<=",
                status,
                startDate,
                endDate,
                validAppointmentCount,
                validAppointmentCount
        );
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

        return buildResponse(
                "TOTAL_DAILY_FOOTFALL",
                "Total Daily Footfall",
                BigDecimal.valueOf(value),
                "patients",
                BigDecimal.ZERO,
                ">=",
                status,
                startDate,
                endDate,
                value,
                value
        );
    }


    // =========================================================
    // 5. UCC Door-to-Doctor Time
    // =========================================================
    public KpiResponse getDoorToDoctorTime(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);

        List<KpiDurationProjection> records = patientEncounterRepository.findDoorToDoctorTimes(startDate, endDate.plusDays(1), EncounterType.EMERGENCY);

        if (records.isEmpty()) {
            return buildNoDataResponse(
                    DOOR_TO_DOCTOR_TIME,
                    DOOR_TO_DOCTOR_LABEL,
                    "min",
                    DOOR_TO_DOCTOR_TARGET,
                    "<=",
                    startDate,
                    endDate
            );
        }

        BigDecimal totalMinutes = records.stream()
                .map(record -> calculateMinutes(
                        record.getStartTime(),
                        record.getEndTime()
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageMinutes = totalMinutes
                .divide(
                        BigDecimal.valueOf(records.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        KpiStatus status =
                averageMinutes.compareTo(DOOR_TO_DOCTOR_TARGET) <= 0
                        ? KpiStatus.ACHIEVED
                        : KpiStatus.NOT_ACHIEVED;

        return buildResponse(
                DOOR_TO_DOCTOR_TIME,
                DOOR_TO_DOCTOR_LABEL,
                averageMinutes,
                "min",
                DOOR_TO_DOCTOR_TARGET,
                "<=",
                status,
                startDate,
                endDate,
                records.size(),
                records.size()
        );
    }

    // =========================================================
    // 6. UCC Length of Stay
    // =========================================================
    public KpiResponse getUccLengthOfStay(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);

        List<KpiDurationProjection> records =
                patientEncounterRepository.findUccLengthOfStay(
                        startDate,
                        endDate.plusDays(1),
                        EncounterType.EMERGENCY
                );

        if (records.isEmpty()) {
            return buildNoDataResponse(
                    UCC_LENGTH_OF_STAY,
                    UCC_LENGTH_OF_STAY_LABEL,
                    "min",
                    UCC_LENGTH_OF_STAY_TARGET,
                    "<=",
                    startDate,
                    endDate
            );
        }

        BigDecimal totalMinutes = records.stream()
                .map(record -> calculateMinutes(
                        record.getStartTime(),
                        record.getEndTime()
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageMinutes = totalMinutes
                .divide(
                        BigDecimal.valueOf(records.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        KpiStatus status =
                averageMinutes.compareTo(UCC_LENGTH_OF_STAY_TARGET) <= 0
                        ? KpiStatus.ACHIEVED
                        : KpiStatus.NOT_ACHIEVED;

        return buildResponse(
                UCC_LENGTH_OF_STAY,
                UCC_LENGTH_OF_STAY_LABEL,
                averageMinutes,
                "min",
                UCC_LENGTH_OF_STAY_TARGET,
                "<=",
                status,
                startDate,
                endDate,
                records.size(),
                records.size()
        );
    }

    // =========================================================
    // 7. UCC Triage Completion Time
    // =========================================================
    public KpiResponse getTriageCompletionTime(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);

        List<KpiDurationProjection> records =
                patientEncounterRepository.findTriageCompletionTimes(
                        startDate,
                        endDate.plusDays(1),
                        EncounterType.EMERGENCY
                );

        if (records.isEmpty()) {
            return buildNoDataResponse(
                    TRIAGE_COMPLETION_TIME,
                    TRIAGE_COMPLETION_LABEL,
                    "min",
                    TRIAGE_COMPLETION_TARGET,
                    "<=",
                    startDate,
                    endDate
            );
        }

        BigDecimal totalMinutes = records.stream()
                .map(record -> calculateMinutes(
                        record.getStartTime(),
                        record.getEndTime()
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageMinutes = totalMinutes
                .divide(
                        BigDecimal.valueOf(records.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        KpiStatus status =
                averageMinutes.compareTo(TRIAGE_COMPLETION_TARGET) <= 0
                        ? KpiStatus.ACHIEVED
                        : KpiStatus.NOT_ACHIEVED;

        return buildResponse(
                TRIAGE_COMPLETION_TIME,
                TRIAGE_COMPLETION_LABEL,
                averageMinutes,
                "min",
                TRIAGE_COMPLETION_TARGET,
                "<=",
                status,
                startDate,
                endDate,
                records.size(),
                records.size()
        );
    }

    // =========================================================
    // 8. UCC Left Without Being Seen
    // =========================================================
    public KpiResponse getLeftWithoutBeingSeen(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);

        LocalDate endExclusive = endDate.plusDays(1);

        List<String> lwbsStatuses = List.of(
                "TRIAGE_STARTED",
                "ASSIGNED_TO_BED",
                "PENDING_PAYMENT",
                "WAITING_TRIAGE",
                "NEW"
        );

        long leftWithoutBeingSeen = patientEncounterRepository.countLeftWithoutBeingSeen(startDate, endExclusive, EncounterType.EMERGENCY, lwbsStatuses);
        long totalUcc = patientEncounterRepository.countEncountersByType(startDate, endExclusive, EncounterType.EMERGENCY);

        if (totalUcc == 0) {
            return buildNoDataResponse(
                    LEFT_WITHOUT_BEING_SEEN,
                    LEFT_WITHOUT_BEING_SEEN_LABEL,
                    "%",
                    LEFT_WITHOUT_BEING_SEEN_TARGET,
                    "<",
                    startDate,
                    endDate
            );
        }

        BigDecimal percentage = BigDecimal.valueOf(leftWithoutBeingSeen)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(totalUcc),
                        2,
                        RoundingMode.HALF_UP
                );

        KpiStatus status = percentage.compareTo(LEFT_WITHOUT_BEING_SEEN_TARGET) < 0
                ? KpiStatus.ACHIEVED
                : KpiStatus.NOT_ACHIEVED;

        return buildResponse(
                LEFT_WITHOUT_BEING_SEEN,
                LEFT_WITHOUT_BEING_SEEN_LABEL,
                percentage,
                "%",
                LEFT_WITHOUT_BEING_SEEN_TARGET,
                "<",
                status,
                startDate,
                endDate,
                leftWithoutBeingSeen,
                totalUcc
        );
    }

    // =========================================================
    // 9. UCC Unplanned Re-attendance <72hr
    // =========================================================
    public KpiResponse getUnplannedReattendance(LocalDate startDate, LocalDate endDate) {

        validateDates(startDate, endDate);


        long reattendanceCount =
                patientEncounterRepository.countUnplannedReattendance(
                        startDate,
                        endDate,
                        EncounterType.EMERGENCY.name()
                );

        long totalDischarges =
                patientEncounterRepository.countDischargesByEncounterType(
                        startDate,
                        endDate,
                        EncounterType.EMERGENCY
                );

        if (totalDischarges == 0) {
            return buildNoDataResponse(
                    "UNPLANNED_REATTENDANCE_72H",
                    "Unplanned Re-attendance <72h",
                    "%",
                    BigDecimal.valueOf(5),
                    "<",
                    startDate,
                    endDate
            );
        }

        BigDecimal percentage = BigDecimal.valueOf(reattendanceCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(totalDischarges),
                        2,
                        RoundingMode.HALF_UP
                );

        KpiStatus status =
                percentage.compareTo(BigDecimal.valueOf(5)) < 0
                        ? KpiStatus.ACHIEVED
                        : KpiStatus.NOT_ACHIEVED;

        return buildResponse(
                "UNPLANNED_REATTENDANCE_72H",
                "Unplanned Re-attendance <72h",
                percentage,
                "%",
                BigDecimal.valueOf(5),
                "<",
                status,
                startDate,
                endDate,
                reattendanceCount,
                totalDischarges
        );
    }

    // =========================================================
    // 10. No show rate appointments per department
    // =========================================================

    public KpiResponse getNoShowRate(LocalDate startDate, LocalDate endDate, Long departmentId) {

        validateDates(startDate, endDate);

        Instant start = toStartOfDay(startDate);
        Instant end = toStartOfDay(endDate.plusDays(1));

        /*
         * Booked appointments for this department.
         *
         * NO_SHOW is included because the appointment was originally booked.
         *
         * CANCELLED is excluded.
         */
        long bookedAppointments =
                appointmentRepository
                        .countAppointmentByStatusInAndDepartmentIdAndStartDatetimeGreaterThanEqualAndEndDatetimeLessThan(
                                getBookedStatuses(),
                                departmentId,
                                start,
                                end
                        );

        /*
         * NO_SHOW appointments for this department.
         */
        long noShowAppointments =
                appointmentRepository
                        .countAppointmentByStatusAndDepartmentIdAndStartDatetimeGreaterThanEqualAndEndDatetimeLessThan(
                                AppointmentStatus.NO_SHOW,
                                departmentId,
                                start,
                                end
                        );

        BigDecimal noShowRate =
                calculatePercentage(
                        noShowAppointments,
                        bookedAppointments
                );

        KpiStatus status;

        if (bookedAppointments == 0) {

            status = KpiStatus.NO_DATA;

        } else if (noShowRate.compareTo(NO_SHOW_TARGET) <= 0) {

            status = KpiStatus.ACHIEVED;

        } else {

            status = KpiStatus.NOT_ACHIEVED;
        }

        return buildResponse(
                DNA_NO_SHOW_RATE,
                "DNA / No-Show Rate",
                noShowRate,
                "%",
                NO_SHOW_TARGET,
                "<=",
                status,
                startDate,
                endDate,
                noShowAppointments,
                bookedAppointments
        );
    }

    // =========================================================
    // 11. Avg Consultation Duration per department
    // =========================================================
    public KpiResponse getAverageConsultationDuration(LocalDate startDate, LocalDate endDate, Long departmentId) {

        validateDates(startDate, endDate);
        if (departmentId == null) {
            return buildNoDataResponse(
                    AVG_CONSULTATION_DURATION,
                    AVG_CONSULTATION_DURATION_LABEL,
                    "min",
                    AVG_CONSULTATION_DURATION_MAX,
                    "12-18",
                    startDate,
                    endDate
            );
        }
        List<KpiDurationProjection> records =
                patientEncounterRepository.findConsultationDurations(
                        startDate,
                        endDate.plusDays(1),
                        departmentId
                );

        if (records.isEmpty()) {
            return buildNoDataResponse(
                    AVG_CONSULTATION_DURATION,
                    AVG_CONSULTATION_DURATION_LABEL,
                    "min",
                    AVG_CONSULTATION_DURATION_MAX,
                    "12-18",
                    startDate,
                    endDate
            );
        }

        BigDecimal totalMinutes = records.stream()
                .map(record -> calculateMinutes(
                        record.getStartTime(),
                        record.getEndTime()
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageMinutes = totalMinutes
                .divide(
                        BigDecimal.valueOf(records.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        KpiStatus status =
                averageMinutes.compareTo(BigDecimal.valueOf(12)) >= 0
                        && averageMinutes.compareTo(BigDecimal.valueOf(18)) <= 0
                        ? KpiStatus.ACHIEVED
                        : KpiStatus.NOT_ACHIEVED;

        return buildResponse(
                AVG_CONSULTATION_DURATION,
                AVG_CONSULTATION_DURATION_LABEL,
                averageMinutes,
                "min",
                AVG_CONSULTATION_DURATION_MAX,
                "12-18",
                status,
                startDate,
                endDate,
                records.size(),
                records.size()
        );
    }

    // =========================================================
    // 12. Chronic Disease Register per department
    // =========================================================
    public KpiResponse getChronicDiseaseRegister(LocalDate startDate, LocalDate endDate, Long departmentId) {

        validateDates(startDate, endDate);

        long chronicDiseasePatients = patientProblemRepository.countChronicDiseasePatientsByDepartment(
                startDate,
                endDate.plusDays(1),
                departmentId
        );

        KpiStatus status =
                chronicDiseasePatients > 0
                        ? KpiStatus.ACHIEVED
                        : KpiStatus.NO_DATA;

        return buildResponse(
                "CHRONIC_DISEASE_REGISTER",
                "Chronic Disease Register",
                BigDecimal.valueOf(chronicDiseasePatients),
                "#",
                null,
                null,
                status,
                startDate,
                endDate,
                chronicDiseasePatients,
                chronicDiseasePatients
        );
    }

    // =========================================================
    // 13. Diabetic HbA1c Monitoring per department
    // =========================================================

    /**
     * Diabetic HbA1c Monitoring
     * <p>
     * Definition:
     * Diabetic patients with HbA1c checked within 6 months.
     * <p>
     * Formula:
     * <p>
     * Diabetic patients with HbA1c within 6 months
     * --------------------------------------------- x 100
     * Total diabetic patients
     * <p>
     * Target: >= 85%
     * <p>
     * Frequency: Quarterly
     */
    public KpiResponse getDiabeticHba1cMonitoring(LocalDate startDate, LocalDate endDate, Long departmentId) {

        validateDates(startDate, endDate);

        /*
         * The KPI population is the diabetic patients
         * associated with the selected department during
         * the KPI period.
         */
        long diabeticPatients =
                patientProblemRepository.countDiabeticPatientsByDepartment(
                        startDate,
                        endDate.plusDays(1),
                        departmentId
                );

        /*
         * HbA1c must have been checked within the previous
         * six months from the KPI end date.
         */
        LocalDate hba1cStartDate =
                endDate.minusMonths(6);

        if (diabeticPatients == 0) {

            return buildResponse(
                    DIABETIC_HBA1C_MONITORING,
                    "Diabetic HbA1c Monitoring",
                    BigDecimal.ZERO,
                    "%",
                    DIABETIC_HBA1C_TARGET,
                    ">=",
                    KpiStatus.NO_DATA,
                    startDate,
                    endDate,
                    0,
                    0
            );
        }

        long diabeticPatientsWithHba1c =
                patientProblemRepository
                        .countDiabeticPatientsWithHba1cWithinSixMonthsByDepartment(
                                startDate,
                                endDate.plusDays(1),
                                hba1cStartDate,
                                departmentId
                        );

        BigDecimal percentage =
                calculatePercentage(
                        diabeticPatientsWithHba1c,
                        diabeticPatients
                );

        KpiStatus status;

        if (percentage.compareTo(DIABETIC_HBA1C_TARGET) >= 0) {

            status = KpiStatus.ACHIEVED;

        } else {

            status = KpiStatus.NOT_ACHIEVED;
        }

        return buildResponse(
                DIABETIC_HBA1C_MONITORING,
                DIABETIC_HBA1C_MONITORING_LABEL,
                percentage,
                "%",
                DIABETIC_HBA1C_TARGET,
                ">=",
                status,
                startDate,
                endDate,
                diabeticPatientsWithHba1c,
                diabeticPatients
        );
    }

    // =========================================================
    // 14. MODALITY UTILISATION - CT
    // =========================================================
    public KpiResponse getModalityUtilisationCt(LocalDate startDate, LocalDate endDate) {

        LOG.debug("[KPIS][CT_UTIL] startDate={} endDate={} analyticsTimezone={}", startDate, endDate, analyticsTimezone);
        validateDates(startDate, endDate);

        List<DiagnosticTest> ctDiagnosticTests =
                diagnosticTestRepository.findByModality("CT");

        List<Long> ctDiagnosticTestIds = ctDiagnosticTests == null
                ? List.of()
                : ctDiagnosticTests.stream()
                .map(DiagnosticTest::getId)
                .toList();
        int ctIdsCount = ctDiagnosticTestIds == null ? 0 : ctDiagnosticTestIds.size();
        List<Long> ctIdSample = ctDiagnosticTestIds == null
                ? List.of()
                : ctDiagnosticTestIds.stream().limit(10).toList();
        LOG.debug("[KPIS][CT_UTIL] ctTestIdsCount={} ctTestIdSample={}", ctIdsCount, ctIdSample);

        if (ctDiagnosticTestIds == null || ctDiagnosticTestIds.isEmpty()) {
            LOG.warn("[KPIS][CT_UTIL] NO_DATA reason=NO_CT_TEST_IDS startDate={} endDate={}", startDate, endDate);
            return buildNoDataResponse(
                    MODALITY_UTILISATION_CT,
                    MODALITY_UTILISATION_CT_LABEL,
                    "%",
                    MODALITY_UTILISATION_CT_TARGET,
                    ">",
                    startDate,
                    endDate
            );
        }

        Instant start = toStartOfDay(startDate);
        Instant end = toStartOfDay(endDate.plusDays(1));
        LOG.debug("[KPIS][CT_UTIL] queryWindowStart={} queryWindowEndExclusive={} resourceType={}",
                start,
                end,
                TemplateType.DIAGNOSTIC_TEST
        );

        long bookedSlots = appointmentRepository
                .countByResourceTypeAndResourceIdInAndStatusInAndStartDatetimeRange(
                        TemplateType.DIAGNOSTIC_TEST,
                        ctDiagnosticTestIds,
                        getBookedStatuses(),
                        start,
                        end
                );

        LOG.debug("[KPIS][CT_UTIL] bookedSlots={} bookedStatuses={}", bookedSlots, getBookedStatuses());

        long unbookedSlots = appointmentRepository
                .countByResourceTypeAndResourceIdInAndStatusAndStartDatetimeRange(
                        TemplateType.DIAGNOSTIC_TEST,
                        ctDiagnosticTestIds,
                        AppointmentStatus.NEW,
                        start,
                        end
                );

        LOG.debug("[KPIS][CT_UTIL] unbookedSlots={} unbookedStatus={}", unbookedSlots, AppointmentStatus.NEW);

        long totalSlots = bookedSlots + unbookedSlots;
        LOG.debug("[KPIS][CT_UTIL] totalSlots={} (booked + unbooked)", totalSlots);

        if (totalSlots == 0) {
            LOG.warn("[KPIS][CT_UTIL] NO_DATA reason=ZERO_TOTAL_SLOTS start={} endExclusive={} ctTestIdsCount={}",
                    start,
                    end,
                    ctIdsCount
            );
            return buildNoDataResponse(
                    MODALITY_UTILISATION_CT,
                    MODALITY_UTILISATION_CT_LABEL,
                    "%",
                    MODALITY_UTILISATION_CT_TARGET,
                    ">=",
                    startDate,
                    endDate
            );
        }

        BigDecimal utilisationRate = calculatePercentage(bookedSlots, totalSlots);

        KpiStatus status = utilisationRate.compareTo(MODALITY_UTILISATION_CT_TARGET) > 0
                ? KpiStatus.ACHIEVED
                : KpiStatus.NOT_ACHIEVED;

        LOG.debug("[KPIS][CT_UTIL] utilisationRate={} target={} operator=> status={} numerator(booked)={} denominator(total)={}",
                utilisationRate,
                MODALITY_UTILISATION_CT_TARGET,
                status,
                bookedSlots,
                totalSlots
        );

        return buildResponse(
                MODALITY_UTILISATION_CT,
                MODALITY_UTILISATION_CT_LABEL,
                utilisationRate,
                "%",
                MODALITY_UTILISATION_CT_TARGET,
                ">=",
                status,
                startDate,
                endDate,
                bookedSlots,
                totalSlots
        );
    }

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

    private BigDecimal calculateMinutes(Instant start, Instant end) {

        if (start == null || end == null) {
            return BigDecimal.ZERO;
        }

        long seconds = Duration.between(start, end).getSeconds();

        return BigDecimal.valueOf(seconds)
                .divide(
                        BigDecimal.valueOf(60),
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

    private KpiResponse buildNoDataWaitTimeResponse(LocalDate startDate, LocalDate endDate) {

        KpiResponse response = new KpiResponse();

        response.setKpi(AVERAGE_WAIT_TIME_SCHEDULED);

        response.setLabel("Average Wait Time (Scheduled)");

        response.setValue(BigDecimal.ZERO);

        response.setUnit("min");

        response.setTarget(AVERAGE_WAIT_TIME_TARGET);

        response.setTargetOperator("<=");

        response.setStatus(KpiStatus.NO_DATA);

        response.setStartDate(startDate);

        response.setEndDate(endDate);

        response.setNumerator(0L);

        response.setDenominator(0L);

        return response;
    }

    private KpiResponse buildResponse(String kpi, String label, BigDecimal value, String unit, BigDecimal target, String targetOperator, KpiStatus status, LocalDate startDate, LocalDate endDate, long numerator, long denominator) {

        KpiResponse response = new KpiResponse();

        response.setKpi(kpi);
        response.setLabel(label);
        response.setValue(value);
        response.setUnit(unit);
        response.setTarget(target);
        response.setTargetOperator(targetOperator);
        response.setStatus(status);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setNumerator(numerator);
        response.setDenominator(denominator);

        return response;
    }

    private KpiResponse buildNoDataResponse(String kpi, String label, String unit, BigDecimal target, String targetOperator, LocalDate startDate, LocalDate endDate) {

        return buildResponse(
                kpi,
                label,
                BigDecimal.ZERO,
                unit,
                target,
                targetOperator,
                KpiStatus.NO_DATA,
                startDate,
                endDate,
                0,
                0
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateDates(LocalDate startDate, LocalDate endDate) {

        if (startDate == null) {

            throw new BadRequestAlertException(
                    "startDate is required", "AnalyticsKpiService", "startDate.required"
            );
        }

        if (endDate == null) {

            throw new BadRequestAlertException(
                    "endDate is required", "AnalyticsKpiService", "endDate.required"
            );
        }

        if (endDate.isBefore(startDate)) {

            throw new BadRequestAlertException(
                    "endDate must be greater than or equal to startDate", "AnalyticsKpiService", "endDate.before.startDate"
            );
        }
    }


}