package com.dazzle.asklepios.service.dto.laboratory;

import java.util.List;

public record LaboratoryOrderSectionDTO(
        Long orderId,
        Long orderNumber,
        String encounterNumber,
        String fromDepartment,

        List<LaboratoryOrderTestSectionDTO> orderTests
) {
}
