package com.dazzle.asklepios.service.dto;

import java.util.List;

/**
 * Result of a grouped sample-label print request: the rendered PDF plus the
 * names of any selected tests that were skipped because they're missing
 * analyzer/tube laboratory configuration.
 */
public record GroupedSampleLabelsPrintResult(
        byte[] pdf,
        List<String> missingDefinitionTestNames
) {
}
