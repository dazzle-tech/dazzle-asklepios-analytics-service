package com.dazzle.asklepios.repository;

import java.time.LocalDate;

public interface DailyPatientEncounterCountProjection {

    LocalDate getDate();

    Long getPatientCount();

    Long getEncounterCount();
}
