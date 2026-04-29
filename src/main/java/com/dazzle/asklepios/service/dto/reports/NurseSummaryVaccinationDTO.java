package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;

public record NurseSummaryVaccinationDTO(
        Long id,
        Long vaccineId,
        Long vaccineBrandId,
        Long vaccineDoseId,
        String vaccineLotNumber,
        Instant dateAdministered,
        String status,
        String administeredLocation,
        String administrationReactions,
        Boolean isExternalFacility,
        String externalFacilityName,
        String notes
) {
}