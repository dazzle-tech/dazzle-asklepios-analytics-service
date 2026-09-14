package com.dazzle.asklepios.repository;

import java.time.Instant;

public interface KpiDurationProjection {

    Long getEncounterId();

    Instant getStartTime();

    Instant getEndTime();
}
