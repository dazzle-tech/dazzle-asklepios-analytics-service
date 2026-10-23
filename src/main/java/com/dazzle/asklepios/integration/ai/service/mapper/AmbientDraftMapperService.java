package com.dazzle.asklepios.integration.ai.service.mapper;

import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.domain.enumeration.ModeOfArrival;
import com.dazzle.asklepios.domain.enumeration.Severity;
import com.dazzle.asklepios.repository.ICDDiagnosisRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AmbientDraftMapperService {

    private static final Logger log = LoggerFactory.getLogger(AmbientDraftMapperService.class);

    private final ICDDiagnosisRepository icdDiagnosisRepository;
    private final EnumMapperService enumMapperService;
    private final ApLovMapperService apLovMapperService;

    public JsonNode map(JsonNode draft) {
        if (!(draft instanceof ObjectNode root)) {
            return draft;
        }

        ArrayNode unresolved = root.putArray("unresolved");
        try {
            mapDiagnoses(root, unresolved);
            mapRecordValues(root, unresolved);
        } catch (RuntimeException e) {
            log.error("Ambient draft mapping failed; returning the draft unmapped", e);
        }
        return root;
    }

    private void mapDiagnoses(ObjectNode root, ArrayNode unresolved) {
        JsonNode diagnoses = root.get("diagnoses");
        if (diagnoses == null || !diagnoses.isArray()) {
            return;
        }
        JsonNode recordDiagnoses = root.path("record").get("patient_diagnosis");

        for (int i = 0; i < diagnoses.size(); i++) {
            if (!(diagnoses.get(i) instanceof ObjectNode diagnosis)) {
                continue;
            }
            String code = text(diagnosis, "icd10");
            String label = text(diagnosis, "label");
            Long id = null;

            if (code == null) {
                unresolved.add(issue("diagnoses." + i, label, "no ICD-10 code", null));
            } else {
                List<ICDDiagnosis> matches = icdDiagnosisRepository.findByIcdCodeIn(codeForms(code));
                if (matches.size() == 1) {
                    id = matches.get(0).getId();
                } else {
                    ArrayNode candidates = root.arrayNode();
                    matches.stream().limit(5).forEach(d -> candidates.add(
                            candidate(d.getId(), d.getIcdCode() + " " + nullToEmpty(d.getIcdShortDescription()))));
                    unresolved.add(issue(
                            "diagnoses." + i,
                            code + " " + nullToEmpty(label),
                            matches.isEmpty() ? "ICD-10 code not found" : "more than one diagnosis has this code",
                            candidates));
                }
            }

            putLong(diagnosis, "diagnosisId", id);
            if (recordDiagnoses != null && recordDiagnoses.isArray()
                    && recordDiagnoses.get(i) instanceof ObjectNode recordDiagnosis) {
                putLong(recordDiagnosis, "diagnosisId", id);
            }
        }
    }

    private void mapRecordValues(ObjectNode root, ArrayNode unresolved) {
        JsonNode record = root.get("record");
        if (record == null || !record.isObject()) {
            return;
        }

        if (record.get("vital_sign") instanceof ObjectNode vitals) {
            mapLovKey(vitals, "measurementSite", AsklepiosLovCodes.BP_MEASUREMENT_SITE,
                    "record.vital_sign", unresolved, root);
            toReadableText(vitals, "fluidStatus");
            toReadableText(vitals, "edema");
            toReadableText(vitals, "urineOutput");
        }
        if (record.get("pain_assessment") instanceof ObjectNode pain) {
            String degree = text(pain, "painDegree");
            if (degree != null) {
                Severity severity = enumMapperService.mapToSeverity(degree);
                putEnum(pain, "painDegree", severity, degree, "record.pain_assessment", unresolved);
            }
            mapLovKey(pain, "painPattern", AsklepiosLovCodes.PAIN_PATTERN,
                    "record.pain_assessment", unresolved, root);
        }
        if (record.get("nursing_assessment") instanceof ObjectNode nursing) {
            String mode = text(nursing, "modeOfArrival");
            if (mode != null) {
                ModeOfArrival modeOfArrival = enumMapperService.mapToModeOfArrival(mode);
                putEnum(nursing, "modeOfArrival", modeOfArrival, mode, "record.nursing_assessment", unresolved);
            }
        }
    }

    private void mapLovKey(ObjectNode group, String field, String lovCode,
                           String groupPath, ArrayNode unresolved, ObjectNode root) {
        String value = text(group, field);
        if (value == null) {
            return;
        }
        String key = apLovMapperService.mapToKey(lovCode, value);
        if (key != null) {
            group.put(field, key);
        } else {
            ArrayNode candidates = root.arrayNode();
            apLovMapperService.findValues(lovCode)
                    .forEach(v -> candidates.add(candidate(v.getKey(), v.getLovDisplayVale())));
            group.putNull(field);
            unresolved.add(issue(groupPath + "." + field, value, "no matching list value", candidates));
        }
    }

    private static void putEnum(ObjectNode group, String field, Enum<?> value, String spoken,
                                String groupPath, ArrayNode unresolved) {
        if (value != null) {
            group.put(field, value.name());
        } else {
            group.putNull(field);
            unresolved.add(issue(groupPath + "." + field, spoken, "no matching value", null));
        }
    }

    private static void toReadableText(ObjectNode group, String field) {
        String value = text(group, field);
        if (value != null) {
            String words = value.replace('_', ' ').toLowerCase(Locale.ROOT);
            group.put(field, Character.toUpperCase(words.charAt(0)) + words.substring(1));
        }
    }

    private static ObjectNode issue(String path, String spoken, String reason, ArrayNode candidates) {
        ObjectNode issue = JsonNodeFactory.instance.objectNode();
        issue.put("path", path);
        issue.put("spoken", spoken);
        issue.put("reason", reason);
        issue.set("candidates", candidates != null ? candidates : issue.arrayNode());
        return issue;
    }

    private static ObjectNode candidate(Object id, String label) {
        ObjectNode candidate = JsonNodeFactory.instance.objectNode();
        candidate.put("id", String.valueOf(id));
        candidate.put("label", label);
        return candidate;
    }

    private static void putLong(ObjectNode node, String field, Long value) {
        if (value != null) {
            node.put(field, value);
        } else {
            node.putNull(field);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asString().trim();
        return text.isEmpty() ? null : text;
    }

    /** I20.8 -> [I20.8, I208] */
    private static Set<String> codeForms(String code) {
        String upper = code.trim().toUpperCase(Locale.ROOT);
        String noDot = upper.replace(".", "");
        Set<String> forms = new LinkedHashSet<>(List.of(upper, noDot));
        if (noDot.length() > 3) {
            forms.add(noDot.substring(0, 3) + "." + noDot.substring(3));
        }
        return forms;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
