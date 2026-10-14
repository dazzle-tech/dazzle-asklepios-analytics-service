package com.dazzle.asklepios.service.dto;

import java.time.Instant;

/**
 * Label shown for the "Print Selected" bulk/grouped print flow - shows
 * analyzer type / tube type / tube color instead of source/amount, since
 * those are the attributes the group was matched on.
 */
public record GroupedSampleLabelDTO(
        Long orderId,
        String patientName,
        String facilityName,
        String mrn,
        String testName,
        Instant sampleDateTime,
        Instant expiryDate,
        String analyzerType,
        String tubeType,
        String tubeColor,
        String printNumber
) {
}
