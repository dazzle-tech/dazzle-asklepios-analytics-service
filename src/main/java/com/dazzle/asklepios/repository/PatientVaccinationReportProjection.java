package com.dazzle.asklepios.repository;

import java.time.Instant;

public interface PatientVaccinationReportProjection {

    Long getVaccineId();

    String getVaccineName();

    String getAtcCode();

    String getVaccineType();

    String getNumberOfDoses();

    String getRoa();

    String getSiteOfAdministration();

    Long getVaccineBrandId();

    Long getVaccineDoseId();

    Instant getDateAdministered();

    String getAdministeredLocation();

    Boolean getIsExternalFacility();

    String getExternalFacilityName();
}
