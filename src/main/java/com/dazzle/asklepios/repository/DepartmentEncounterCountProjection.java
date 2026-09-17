package com.dazzle.asklepios.repository;

import java.math.BigDecimal;

public interface DepartmentEncounterCountProjection {

    String getDepartmentName();

    Long getEncounterCount();

    BigDecimal getPercentage();
}
