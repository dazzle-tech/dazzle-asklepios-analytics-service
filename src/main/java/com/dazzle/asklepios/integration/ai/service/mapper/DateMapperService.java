package com.dazzle.asklepios.integration.ai.service.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
public class DateMapperService {

    private static final Logger LOG = LoggerFactory.getLogger(DateMapperService.class);

    // Formats found on ID cards and passports; month names are English and case-insensitive ("12 APR 1997")
    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            caseInsensitive("dd/MM/yyyy"),
            caseInsensitive("d/M/yyyy"),
            caseInsensitive("dd-MM-yyyy"),
            caseInsensitive("d-M-yyyy"),
            caseInsensitive("dd.MM.yyyy"),
            caseInsensitive("yyyy/MM/dd"),
            caseInsensitive("d MMM yyyy"),
            caseInsensitive("d MMMM yyyy"),
            caseInsensitive("d-MMM-yyyy"),
            caseInsensitive("MMM d, yyyy"),
            caseInsensitive("MMMM d, yyyy")
    );

    private static DateTimeFormatter caseInsensitive(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH);
    }

  
    public LocalDate parseDateOfBirth(String input) {
        if (input == null) return null;

        String trimmed = input.trim().replaceAll("\\s+", " ");
        String normalized = trimmed.toLowerCase(Locale.ROOT);

        if (normalized.isBlank()
                || normalized.equals("undefined")
                || normalized.equals("null")
                || normalized.equals("-")
                || normalized.equals("n/a")) {
            return null;
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }

        LOG.warn("[OCR_DATE] Unsupported date of birth format, leaving it empty: {}", input);
        return null;
    }
}
