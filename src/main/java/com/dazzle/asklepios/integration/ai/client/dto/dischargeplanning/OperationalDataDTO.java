package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OperationalDataDTO(
        @JsonProperty("follow_up_appointments")
        List<FollowUpAppointmentDTO> followUpAppointments,

        @JsonProperty("patient_education")
        List<PatientEducationDTO> patientEducation,

        @JsonProperty("transport_status")
        String transportStatus,

        @JsonProperty("home_support")
        String homeSupport,

        @JsonProperty("equipment_needs")
        List<EquipmentNeedDTO> equipmentNeeds
) {}
