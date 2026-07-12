package com.dazzle.asklepios.integration.ai.client.dto;

import com.dazzle.asklepios.domain.enumeration.Gender;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalDate;

public record PatientInfoResponseDTO(
        @JsonAlias("Type") String type,
        @JsonAlias("Document Number") String documentNumber,
//        @JsonProperty("Surname / Family Name") String familyName,
        @JsonAlias({"Surname/Family Name", "Surname / Family Name"}) String familyName,
        @JsonAlias("Given Names") String givenNames,
        @JsonAlias("Nationality") String nationality,
        @JsonAlias("Date of Birth") LocalDate dateOfBirth,
        @JsonAlias("Sex") Gender sex,
        @JsonAlias("Place of Birth") String placeOfBirth
) {

}
