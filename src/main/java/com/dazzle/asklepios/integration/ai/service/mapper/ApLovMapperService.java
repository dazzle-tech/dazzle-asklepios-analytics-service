package com.dazzle.asklepios.integration.ai.service.mapper;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ApLovMapperService {

    private final ApLovValueRepository apLovValueRepository;

    /**
     * Map nationality text (OCR / LLM) -> LOV key
     */
    public String mapNationalityToKey(String nationality) {

        if (nationality == null || nationality.isBlank()) {
            return null;
        }

        String input = normalize(nationality);

        List<ApLovValue> values =
                apLovValueRepository.findByLovCodeAndIsValidTrue("NAT");

        // 1) direct smart match
        Optional<ApLovValue> matched = values.stream()
                .filter(v -> matches(input, v))
                .findFirst();

        if (matched.isPresent()) {
            return matched.get().getKey();
        }

        // 2) fallback: exact code match (NAT_019 etc)
        return values.stream()
                .filter(v -> v.getValueCode() != null)
                .filter(v -> normalize(v.getValueCode()).equals(input))
                .map(ApLovValue::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Main matching logic
     */
    private boolean matches(String input, ApLovValue value) {

        String dbValue = firstNonNull(
                value.getLovDisplayVale(),
                value.getValueDescription(),
                value.getValueCode()
        );

        if (dbValue == null) return false;

        String normalizedDb = normalize(dbValue);

        // exact match
        if (normalizedDb.equals(input)) {
            return true;
        }

        // contains both directions
        if (normalizedDb.contains(input) || input.contains(normalizedDb)) {
            return true;
        }

        // alias matching (USA, UK, UAE etc)
        return aliasMatch(input, normalizedDb);
    }

    /**
     * Handles common OCR / LLM variations WITHOUT hardcoding every country
     */
    private boolean aliasMatch(String input, String dbValue) {

        Map<String, List<String>> aliases = getNationalityAliases();

        for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {

            String canonical = normalize(entry.getKey());
            List<String> variants = entry.getValue();

            boolean inputMatches = variants.stream()
                    .map(this::normalize)
                    .anyMatch(input::contains);

            boolean dbMatches = variants.stream()
                    .map(this::normalize)
                    .anyMatch(dbValue::contains);

            if ((inputMatches && dbValue.contains(canonical)) ||
                    (dbMatches && input.contains(canonical))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Generic normalization
     */
    private String normalize(String value) {
        return value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("_", " ")
                .replace("-", " ")
                .replaceAll("\\s+", " ");
    }

    /**
     * Safe getter
     */
    private String firstNonNull(String... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Central alias registry (NOT per-country logic in code flow)
     */
    private Map<String, List<String>> getNationalityAliases() {

        Map<String, List<String>> map = new HashMap<>();

        map.put("UNITED STATES", List.of("USA", "US", "UNITED STATES", "AMERICA", "AMERICAN"));
        map.put("UNITED KINGDOM", List.of("UK", "BRITAIN", "ENGLAND", "BRITISH"));
        map.put("UNITED ARAB EMIRATES", List.of("UAE", "EMIRATES", "EMIRATI"));
        map.put("SAUDI ARABIA", List.of("SAUDI", "KSA"));
        map.put("PALESTINE", List.of("PALESTINIAN", "PALESTINE"));
        map.put("JORDAN", List.of("JORDANIAN"));
        map.put("EGYPT", List.of("EGYPTIAN"));

        return map;
    }
}