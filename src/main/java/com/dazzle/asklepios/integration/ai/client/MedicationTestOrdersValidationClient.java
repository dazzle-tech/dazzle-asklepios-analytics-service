package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.MedicationValidationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.TestValidationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.ValidationResponseDTO;
import com.dazzle.asklepios.integration.ai.config.MedicationTestOrdersValidationFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "medicationTestOrdersValidationClient",
        url = "${ai.medication-test-orders-validation.base-url}",
        configuration = MedicationTestOrdersValidationFeignConfig.class
)
public interface MedicationTestOrdersValidationClient {

    @PostMapping("/validate/medication")
    ValidationResponseDTO validateMedication(@RequestBody MedicationValidationRequestDTO request);

    @PostMapping("/validate/tests")
    ValidationResponseDTO validateTests(@RequestBody TestValidationRequestDTO request);
}
