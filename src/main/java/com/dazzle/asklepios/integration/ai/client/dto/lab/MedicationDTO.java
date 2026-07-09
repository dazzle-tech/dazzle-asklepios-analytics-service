package com.dazzle.asklepios.integration.ai.client.dto.lab;

import java.time.Instant;
import java.util.Date;

public record MedicationDTO(
        String name,
        Date start_date,
        Instant end_date
) {}