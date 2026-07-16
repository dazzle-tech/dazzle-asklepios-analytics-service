package com.dazzle.asklepios.integration.ai.service.mapper;

import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.JobRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EnumMapperService {

    public String mapToAiUserRole(JobRole jobRole) {
        if (jobRole == null) {
            return "doctor";
        }

        switch (jobRole) {
            case NURSE:
            case MIDWIFE:
                return "nurse";

            case PHYSICIAN:
            case DENTIST:
            case ANESTHESIOLOGIST:
            case RADIOLOGIST:
            case PATHOLOGIST:
            case PHARMACIST:
            case PHYSICAL_THERAPIST:
            case OCCUPATIONAL_THERAPIST:
            case DIETITIAN_NUTRITIONIST:
            case PSYCHOLOGIST:
            case PSYCHIATRIST:
            case PARAMEDIC_EMS:
            case RESPIRATORY_THERAPIST:
            case MEDICAL_DIRECTOR:
                return "doctor";

            default:
                return "admin";
        }
    }

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
