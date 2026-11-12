package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportVerificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Answers the public "is this procedure report genuine?" check behind the report QR code.
 * The signature is checked before any database read, so unsigned ids reveal nothing.
 */
@Service
@Transactional(readOnly = true)
public class ProcedureReportVerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureReportVerificationService.class);

    private final ReportVerificationService reportVerificationService;
    private final JdbcTemplate jdbc;

    public ProcedureReportVerificationService(ReportVerificationService reportVerificationService, JdbcTemplate jdbc) {
        this.reportVerificationService = reportVerificationService;
        this.jdbc = jdbc;
    }

    public ProcedureReportVerificationDTO verify(String token) {
        return reportVerificationService.verify(token)
                .map(v -> lookup(v.procedureRequestId(), v.issuedAt()))
                .orElseGet(() -> {
                    LOG.info("[ProcedureReportVerification] rejected token");
                    return ProcedureReportVerificationDTO.invalid();
                });
    }

    private ProcedureReportVerificationDTO lookup(Long id, Instant issuedAt) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT pp.id, pp.status, p.name AS procedure_name,
                       pa.first_name, pa.second_name, pa.third_name, pa.last_name, pa.medical_record_number,
                       COALESCE(f.name, df.name) AS facility_name,
                       perf.actual_start_time, perf.actual_end_time
                FROM patient_procedure pp
                LEFT JOIN procedure p ON p.id = pp.procedure_id
                LEFT JOIN patients pa ON pa.id = pp.patient_id
                LEFT JOIN facility f ON f.id = pp.to_facility_id
                LEFT JOIN department d ON d.id = pp.from_department_id
                LEFT JOIN facility df ON df.id = d.facility_id
                LEFT JOIN LATERAL (
                    SELECT actual_start_time, actual_end_time
                    FROM patient_procedure_performance
                    WHERE patient_procedure_id = pp.id
                    ORDER BY id DESC LIMIT 1
                ) perf ON TRUE
                WHERE pp.id = ?
                """, id);
        if (rows.isEmpty()) {
            LOG.info("[ProcedureReportVerification] signed token for missing procedure id={}", id);
            return ProcedureReportVerificationDTO.invalid();
        }
        Map<String, Object> r = rows.get(0);
        Instant performed = Objects.requireNonNullElse(instant(r.get("actual_end_time")), instant(r.get("actual_start_time")));
        return new ProcedureReportVerificationDTO(
                true,
                "PRC-" + String.format(Locale.ROOT, "%06d", id),
                text(r.get("facility_name")),
                text(r.get("procedure_name")),
                text(r.get("status")),
                initials(r.get("first_name"), r.get("second_name"), r.get("third_name"), r.get("last_name")),
                maskMrn(text(r.get("medical_record_number"))),
                performed,
                issuedAt
        );
    }

    /** "masa nabeel zahran" becomes "M. N. Z." */
    static String initials(Object... names) {
        String value = Stream.of(names)
                .map(ProcedureReportVerificationService::text)
                .filter(n -> n != null && !n.isBlank())
                .map(n -> n.trim().substring(0, 1).toUpperCase(Locale.ROOT) + ".")
                .collect(Collectors.joining(" "));
        return value.isEmpty() ? null : value;
    }

    /** Keeps the first two and the last character: "P14738" becomes "P1***8". */
    static String maskMrn(String mrn) {
        if (mrn == null || mrn.isBlank()) {
            return null;
        }
        String v = mrn.trim();
        if (v.length() <= 3) {
            return v.charAt(0) + "*".repeat(v.length() - 1);
        }
        return v.substring(0, 2) + "*".repeat(v.length() - 3) + v.charAt(v.length() - 1);
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    private static Instant instant(Object value) {
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime().toInstant(ZoneOffset.UTC);
        }
        return value instanceof Instant i ? i : null;
    }
}
