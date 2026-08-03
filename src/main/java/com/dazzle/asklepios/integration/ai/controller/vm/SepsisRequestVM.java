package com.dazzle.asklepios.integration.ai.controller.vm;

import java.util.List;

public record SepsisRequestVM (
        Long patientId,
        List<String> hourlyData
){
}
