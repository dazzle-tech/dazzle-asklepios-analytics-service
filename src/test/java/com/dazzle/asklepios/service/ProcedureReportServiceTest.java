package com.dazzle.asklepios.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcedureReportServiceTest {

    @Test
    void formatDuration_usesLargestUnitsAndSkipsZeroParts() {
        assertEquals("0m", ProcedureReportService.Fmt.formatDuration(Duration.ofSeconds(40), false));
        assertEquals("45m", ProcedureReportService.Fmt.formatDuration(Duration.ofMinutes(45), false));
        assertEquals("2h", ProcedureReportService.Fmt.formatDuration(Duration.ofHours(2), false));
        assertEquals("1h 30m", ProcedureReportService.Fmt.formatDuration(Duration.ofMinutes(90), false));
        assertEquals("1d 2h",ProcedureReportService.Fmt.formatDuration(Duration.ofMinutes(24 * 60 + 125), false));
        assertEquals("1 س 30 د", ProcedureReportService.Fmt.formatDuration(Duration.ofMinutes(90), true));
    }

    @Test
    void duration_isDashWhenAnEndIsMissingOrBeforeTheStart() {
        ProcedureReportService.Fmt fmt = new ProcedureReportService.Fmt(ZoneId.of("UTC"), false);
        Instant start = Instant.parse("2026-11-05T03:00:00Z");

        assertEquals("—", fmt.duration(start, null));
        assertEquals("—", fmt.duration(start, start.minusSeconds(60)));
        assertEquals("2h", fmt.duration(start, start.plusSeconds(7200)));
    }

    @Test
    void dateTime_isShownInTheRequestedZone() {
        ProcedureReportService.Fmt fmt = new ProcedureReportService.Fmt(ZoneId.of("Asia/Riyadh"), false);

        assertEquals("05/11/2026 06:00", fmt.dateTime(Instant.parse("2026-11-05T03:00:00Z")));
        assertEquals("—", fmt.dateTime(null));
    }

    @Test
    void icdLabel_doesNotRepeatTheCode() {
        assertEquals("M71.2 — Synovial cyst", ProcedureReportService.icdLabel("M71.2", "Synovial cyst"));
        assertEquals("Synovial cyst (M71.2)", ProcedureReportService.icdLabel("M71.2", "Synovial cyst (M71.2)"));
        assertEquals("M71.2", ProcedureReportService.icdLabel("M71.2", null));
    }

    @Test
    void tones_followStatusAndSeverity() {
        assertEquals("success", ProcedureReportService.statusTone("COMPLETED"));
        assertEquals("danger", ProcedureReportService.statusTone("CANCELLED"));
        assertEquals("warning", ProcedureReportService.statusTone("AWAITING_CONSENT"));
        assertEquals("info", ProcedureReportService.statusTone("STARTED"));
        assertEquals("neutral", ProcedureReportService.statusTone(null));

        assertEquals("danger", ProcedureReportService.severityTone("Severe"));
        assertEquals("danger", ProcedureReportService.severityTone("SEVERE"));
        assertEquals("warning", ProcedureReportService.severityTone("Moderate"));
        assertEquals("info", ProcedureReportService.severityTone("Mild/Minor"));
        assertEquals("neutral", ProcedureReportService.severityTone("Unknown"));
    }

    @Test
    void decimal_dropsTrailingZeros() {
        assertEquals("3", ProcedureReportService.decimal(new BigDecimal("3.00")));
        assertEquals("36.5", ProcedureReportService.decimal(new BigDecimal("36.50")));
        assertEquals("120", ProcedureReportService.decimal(new BigDecimal("1.2E+2")));
    }
}
