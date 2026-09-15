package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.AnalyticsKpiService;
import com.dazzle.asklepios.web.rest.vm.kpis.KpiResponse;
import com.dazzle.asklepios.web.rest.vm.report.totalDailyFootfall.TotalDailyFootfallResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsKpiController {

    private static final Logger LOG = LoggerFactory.getLogger(VisitReportController.class);
    private final AnalyticsKpiService analyticsKpiService;

    public AnalyticsKpiController(AnalyticsKpiService analyticsKpiService) {
        this.analyticsKpiService = analyticsKpiService;
    }

    // =========================================================
    // 1. FACILITY UTILIZATION RATE
    // =========================================================

    /**
     * GET /api/analytics/kpis/facility-utilization-rate
     * <p>
     * Example:
     * /api/analytics/kpis/facility-utilization-rate
     * ?startDate=2026-09-01
     * &endDate=2026-09-07
     */
    @GetMapping("/kpis/facility-utilization-rate")
    public ResponseEntity<KpiResponse> getFacilityUtilizationRate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LOG.debug("[KPIS] request facility utilization rate from {} to {}", startDate, endDate);
        KpiResponse response = analyticsKpiService.getFacilityUtilizationRate(startDate, endDate);

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // 2. DNA / NO-SHOW RATE
    // =========================================================

    /**
     * GET /api/analytics/kpis/no-show-rate
     * <p>
     * Example:
     * /api/analytics/kpis/no-show-rate
     * ?startDate=2026-09-01
     * &endDate=2026-09-07
     */
    @GetMapping("/kpis/no-show-rate")
    public ResponseEntity<KpiResponse> getNoShowRate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LOG.debug("[KPIS] request no-show rate from {} to {}", startDate, endDate);
        KpiResponse response = analyticsKpiService.getNoShowRate(startDate, endDate);

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // 3. AVERAGE WAIT TIME - SCHEDULED
    // =========================================================

    /**
     * GET /api/analytics/kpis/average-wait-time
     * <p>
     * Example:
     * /api/analytics/kpis/average-wait-time
     * ?startDate=2026-09-01
     * &endDate=2026-09-07
     */
    @GetMapping("/kpis/average-wait-time")
    public ResponseEntity<KpiResponse> getAverageWaitTime(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LOG.debug("[KPIS] request average wait time from {} to {}", startDate, endDate);
        KpiResponse response = analyticsKpiService.getAverageWaitTime(startDate, endDate);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/kpis/total-daily-footfall")
    public ResponseEntity<KpiResponse> getTotalDailyFootfall(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        LOG.debug("[KPIS] request total daily footfall from {} to {}", startDate, endDate);
        return ResponseEntity.ok(
                analyticsKpiService.getTotalDailyFootfall(startDate, endDate)
        );
    }

    @GetMapping("/kpis/door-to-doctor-time")
    public ResponseEntity<KpiResponse> getDoorToDoctorTime(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                analyticsKpiService.getDoorToDoctorTime(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/kpis/ucc-length-of-stay")
    public ResponseEntity<KpiResponse> getUccLengthOfStay(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                analyticsKpiService.getUccLengthOfStay(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/kpis/triage-completion-time")
    public ResponseEntity<KpiResponse> getTriageCompletionTime(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                analyticsKpiService.getTriageCompletionTime(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/kpis/left-without-being-seen")
    public ResponseEntity<KpiResponse> getLeftWithoutBeingSeen(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                analyticsKpiService.getLeftWithoutBeingSeen(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/kpis/unplanned-reattendance")
    public ResponseEntity<KpiResponse> getUnPlannedReattendance(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                analyticsKpiService.getUnplannedReattendance(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/kpis/no-show-rate/department")
    public ResponseEntity<KpiResponse> getNoShowRate(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate, @RequestParam Long departmentId) {

        return ResponseEntity.ok(
                analyticsKpiService.getNoShowRate(
                        startDate,
                        endDate,
                        departmentId
                )
        );
    }

    @GetMapping("/kpis/average-consultation-duration")
    public ResponseEntity<KpiResponse> getAverageConsultationDuration(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate, @RequestParam Long departmentId) {

        return ResponseEntity.ok(
                analyticsKpiService.getAverageConsultationDuration(
                        startDate,
                        endDate,
                        departmentId
                )
        );
    }

    @GetMapping("/chronic-disease-register")
    public ResponseEntity<KpiResponse> getChronicDiseaseRegister(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Long departmentId) {

        return ResponseEntity.ok(
                analyticsKpiService.getChronicDiseaseRegister(
                        startDate,
                        endDate,
                        departmentId
                )
        );
    }

    @GetMapping("/diabetic-hba1c-monitoring")
    public ResponseEntity<KpiResponse> getDiabeticHba1cMonitoring(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Long departmentId) {

        return ResponseEntity.ok(
                analyticsKpiService.getDiabeticHba1cMonitoring(
                        startDate,
                        endDate,
                        departmentId
                )
        );
    }

    @GetMapping("/kpis/modality-utilisation-ct")
    public ResponseEntity<KpiResponse> getModalityUtilisationCt(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LOG.debug("[KPIS] request modality utilisation CT from {} to {}", startDate, endDate);
        return ResponseEntity.ok(analyticsKpiService.getModalityUtilisationCt(startDate, endDate));
    }
}