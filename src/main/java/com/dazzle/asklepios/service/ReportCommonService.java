package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class
ReportCommonService {

    private final UserRepository userRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final LovLookupService lovLookupService;
    private final DepartmentsRepository departmentRepository;
    public String getDisplayUserName(String login) {
        if (login == null || login.isBlank()) {
            return "";
        }

        return userRepository.findByLogin(login)
                .map(user -> {
                    String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
                    String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
                    String fullName = (firstName + " " + lastName).trim();

                    return fullName.isEmpty() ? login : fullName;
                })
                .orElse(login);
    }

    public String getLovDisplayValue(String lovValueId) {
        if (lovValueId == null || lovValueId.isBlank()) {
            return "";
        }

        return apLovValueRepository.findById(lovValueId)
                .map(ApLovValue::getLovDisplayVale)
                .orElse(lovValueId);
    }

    public String calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }

        LocalDate birthDate = dateOfBirth.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        Period period = Period.between(birthDate, LocalDate.now());

        return period.getYears() + " Years " +
                period.getMonths() + " Months " +
                period.getDays() + " Days";
    }

    public String getPatientDisplayName(Patient patient) {
        if (patient == null) {
            return "";
        }

        return Stream.of(
                        patient.getFirstName(),
                        patient.getSecondName(),
                        patient.getThirdName(),
                        patient.getLastName()
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    public String getDepartmentName(Long departmentId) {
       Department department= departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "departments",
                        "Department not found with id " + departmentId
                ));
        return department != null ? department.getName() : "";
    }

    public String getFacilityNameFromDepartment(Long departmentId) {
        Department department= departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "departments",
                        "Department not found with id " + departmentId
                ));
        if (department == null || department.getFacility() == null) {
            return "";
        }

        return department.getFacility().getName();
    }

    public String getLovDisplayValues(String commaSeparatedKeys) {
        if (commaSeparatedKeys == null || commaSeparatedKeys.isBlank()) {
            return null;
        }

        List<String> keys = Arrays.stream(commaSeparatedKeys.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (keys.isEmpty()) {
            return null;
        }

        Map<String, String> lovMap = lovLookupService.findDisplayValues(keys);

        List<String> resolved = keys.stream()
                .map(lovMap::get)
                .filter(Objects::nonNull)
                .toList();

        return resolved.isEmpty() ? null : String.join(", ", resolved);
    }
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    public String formatDateTime(
            Instant instant,
            String timezone
    ) {
        if (instant == null) {
            return null;
        }

        ZoneId zone = (timezone == null || timezone.isBlank())
                ? ZoneId.systemDefault()
                : ZoneId.of(timezone);

        return instant.atZone(zone)
                .format(DATE_TIME_FORMATTER);
    }
}