package com.dazzle.asklepios.service.dto.reports;

import java.math.BigDecimal;

public record DepartmentEncounterCountDTO(
        String departmentName,
        Long encounterCount,
        BigDecimal percentage
) {}
