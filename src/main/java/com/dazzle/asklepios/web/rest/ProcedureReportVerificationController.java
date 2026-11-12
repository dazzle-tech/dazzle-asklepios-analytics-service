package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.ProcedureReportVerificationService;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportVerificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public (no login) check behind the QR code printed on procedure reports. Always answers 200 so
 * an invalid token cannot be told apart from a missing procedure.
 */
@RestController
@RequestMapping("/api/analytics/public")
@RequiredArgsConstructor
public class ProcedureReportVerificationController {

    private final ProcedureReportVerificationService verificationService;

    @GetMapping("/procedure-report/verify")
    public ResponseEntity<ProcedureReportVerificationDTO> verify(@RequestParam("t") String token) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(verificationService.verify(token));
    }
}
