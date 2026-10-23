package com.dazzle.asklepios.integration.ai.service.mapper;

import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.ModeOfArrival;
import com.dazzle.asklepios.domain.enumeration.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EnumMapperService {

    public Gender mapToGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return null;
        }

        String normalized = gender.trim()
                .toUpperCase(Locale.ROOT);

        switch (normalized) {
            case "M":
            case "MALE":
            case "MAN":
            case "BOY":
                return Gender.MALE;

            case "F":
            case "FEMALE":
            case "WOMAN":
            case "GIRL":
                return Gender.FEMALE;

            default:
                return null;
        }
    }

    public Severity mapToSeverity(String degree) {
        if (degree == null || degree.trim().isEmpty()) {
            return null;
        }

        switch (degree.trim().toUpperCase(Locale.ROOT)) {
            case "NOTHING":
            case "NONE":
                return Severity.NOTHING;

            case "MILD":
            case "MILD_MINOR":
            case "MINOR":
                return Severity.MILD_MINOR;

            case "MODERATE":
                return Severity.MODERATE;

            case "SEVERE":
                return Severity.SEVERE;

            default:
                return null;
        }
    }

    public ModeOfArrival mapToModeOfArrival(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return null;
        }

        switch (mode.trim().toUpperCase(Locale.ROOT)) {
            case "WALK_IN":
            case "WALKED_IN":
            case "AMBULATORY":
                return ModeOfArrival.AMBULATORY;

            case "WHEELCHAIR":
                return ModeOfArrival.WHEELCHAIR;

            case "STRETCHER":
                return ModeOfArrival.STRETCHER;

            case "OTHER":
            case "AMBULANCE":
                return ModeOfArrival.OTHER;

            default:
                return null;
        }
    }
}
