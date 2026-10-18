package com.dazzle.asklepios.web.rest.vm.report;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        String module,
        String templateType,
        String jobRole,
        String userIds,
        @JsonProperty("isUserDefault") Boolean isUserDefault
) {
}
