package com.dazzle.asklepios.integration.ai.service.mapper;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class DateMapperService {

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    public LocalDate parseDateOfBirth(String input) {
        if (input == null) return null;

        String normalized = input.trim().toLowerCase();

        if (normalized.isBlank()
                || normalized.equals("undefined")
                || normalized.equals("null")
                || normalized.equals("-")
                || normalized.equals("n/a")) {
            return null;
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(input.trim(), formatter);
            } catch (DateTimeParseException ignored) {}
        }

        throw new IllegalArgumentException("Unsupported date format: " + input);
    }
}