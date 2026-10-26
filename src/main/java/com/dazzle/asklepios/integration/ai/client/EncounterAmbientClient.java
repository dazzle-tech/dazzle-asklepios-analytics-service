package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.config.PatientServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "encounterAmbientClient",
    url = "${service.asklepios-patient-service-url:http://asklepios-patient-service:8080}",
    configuration = PatientServiceFeignConfig.class
)
public interface EncounterAmbientClient {

    @PutMapping(value = "/api/patient/encounter-ambient/{id}/draft", consumes = MediaType.APPLICATION_JSON_VALUE)
    void updateDraft(@PathVariable("id") Long id, @RequestBody String body);
}
