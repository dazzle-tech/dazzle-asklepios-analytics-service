package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;

public interface AppointmentWaitTimeProjection {

    Instant getCheckedInAt();

    Instant getEncounterStartDate();
}
