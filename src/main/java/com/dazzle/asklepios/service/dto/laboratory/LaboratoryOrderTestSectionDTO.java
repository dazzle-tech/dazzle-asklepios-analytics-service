package com.dazzle.asklepios.service.dto.laboratory;

import java.util.List;

public record LaboratoryOrderTestSectionDTO(
        Long orderTestId,
        String testName,
        String receivedDepartment,

        List<LaboratoryResultItemDTO> results
) {
}