package com.dazzle.asklepios.service.dto.reportTemplate;

import com.dazzle.asklepios.domain.enumeration.Modules;

public record StimulsoftReportTemplateWriteDTO(
        Long id,
        String code,
        String name,
        String description,
        String templateJson,
        Boolean isActive,
        Long facilityId,
        String departmentIds,
        Modules module
) {
}
