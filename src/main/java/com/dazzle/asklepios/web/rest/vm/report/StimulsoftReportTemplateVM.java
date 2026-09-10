package com.dazzle.asklepios.web.rest.vm.report;

import com.dazzle.asklepios.domain.enumeration.Modules;

import java.time.Instant;

public record StimulsoftReportTemplateVM(
        Long id,
        String code,
        String name,
        String description,
        String templateJson,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate,
        Long facilityId,
        String departmentIds,
        Modules module
) {
}
