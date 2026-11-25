package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

public record TestOrdersLookupRequestDTO(
        Long patientId,
        Long encounterId,
        Long orderNumber
) {
}
