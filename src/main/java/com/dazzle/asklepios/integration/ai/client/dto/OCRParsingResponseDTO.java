package com.dazzle.asklepios.integration.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record OCRParsingResponseDTO(
        @JsonAlias("Type") String type,
        @JsonAlias("Document Number") String documentNumber,
//        @JsonProperty("Surname / Family Name") String familyName,
        @JsonAlias({"Surname/Family Name", "Surname / Family Name"}) String familyName,
        @JsonAlias("Given Names") String givenNames,
        @JsonAlias("Nationality") String nationality,
        @JsonAlias("Date of Birth") String dateOfBirth,
        @JsonAlias("Sex") String sex,
        @JsonAlias("Place of Birth") String placeOfBirth
) {
}
