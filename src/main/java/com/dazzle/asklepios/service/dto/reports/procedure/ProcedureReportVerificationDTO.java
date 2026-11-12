package com.dazzle.asklepios.service.dto.reports.procedure;

import java.time.Instant;

/**
 * Public answer for a scanned report QR code. Holds only what a reader needs to compare against
 * the paper copy: no full patient name, no clinical content.
 *
 * @param statusCode current {@code ProcStatus} of the procedure, which may differ from the printed one
 */
public record ProcedureReportVerificationDTO(
        boolean valid,
        String documentNumber,
        String facilityName,
        String procedureName,
        String statusCode,
        String patientInitials,
        String maskedMrn,
        Instant performedAt,
        Instant issuedAt
) {

    public static ProcedureReportVerificationDTO invalid() {
        return new ProcedureReportVerificationDTO(false, null, null, null, null, null, null, null, null);
    }
}
