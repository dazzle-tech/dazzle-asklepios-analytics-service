package com.dazzle.asklepios.integration.ai.service.mapper;

import com.dazzle.asklepios.domain.enumeration.Gender;
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
}
