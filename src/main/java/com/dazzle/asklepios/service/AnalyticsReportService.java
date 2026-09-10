package com.dazzle.asklepios.service;

import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stimulsoft.report.StiReport;
import com.stimulsoft.report.dictionary.StiDataColumn;
import com.stimulsoft.report.dictionary.data.DataRow;
import com.stimulsoft.report.dictionary.data.DataTable;
import com.stimulsoft.report.dictionary.databases.StiDatabase;
import com.stimulsoft.report.dictionary.databases.StiJsonDatabase;
import com.stimulsoft.report.export.service.StiPdfExportService;
import com.stimulsoft.report.export.settings.StiPdfExportSettings;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsReportService {

    private final StimulsoftReportTemplateService reportTemplateService;

    @Value("${report.api-base-url}")
    private String reportApiBaseUrl;

    public byte[] generatePdf(String templateCode, Map<String, String> parameters) {

        log.info("[REPORT] Generating PDF templateCode={}, parameters={}", templateCode, parameters);

        try {
            // 1. Load MRT
            String mrtContent = reportTemplateService.getTemplateContent(templateCode);

            // 2. Create Stimulsoft report
            StiReport report = new StiReport();

            // 3. Load MRT

            report.loadFromJson(mrtContent);

            fixRelativeDataSourceUrls(report);
            // 4. Apply parameters
            applyParameters(report, parameters);

            // 5. Render
            attachCurrentUserAuth(report);
            fillJsonDatabases(report, parameters);  // not setHeadersString

            report.render();

            // 6. Export PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            StiPdfExportService pdfExportService = new StiPdfExportService();

            StiPdfExportSettings settings = new StiPdfExportSettings();

            pdfExportService.exportPdf(report, outputStream, settings);

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("[REPORT] Failed to generate PDF templateCode={}", templateCode, e);

            throw new BadRequestAlertException(
                    "Failed to generate report PDF exception: " + e.getMessage(),
                    "report",
                    "report.generate.failure"
            );
        }
    }

    private void applyParameters(StiReport report, Map<String, String> parameters) {

        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : parameters.entrySet()) {

            String name = entry.getKey();
            String value = entry.getValue();

            log.debug(
                    "[REPORT] Parameter {}={}",
                    name,
                    value
            );

            // Set Stimulsoft variable
            report.setVariable(name, value);
        }
    }

    private void fixRelativeDataSourceUrls(StiReport report) {

        if (report.getDictionary() == null
                || report.getDictionary().getDatabases() == null) {
            return;
        }

        report.getDictionary()
                .getDatabases()
                .forEach(database -> {

                    if (database instanceof StiJsonDatabase jsonDatabase) {

                        String path = jsonDatabase.getPathData();

                        if (path == null || path.isBlank()) {
                            return;
                        }

                        if (path.startsWith("/")) {

                            String absoluteUrl =
                                    reportApiBaseUrl + path;

                            log.info(
                                    "[REPORT] Replacing relative datasource URL: {} -> {}",
                                    path,
                                    absoluteUrl
                            );

                            jsonDatabase.setPathData(absoluteUrl);
                        }
                    }
                });
    }

    private void attachCurrentUserAuth(StiReport report) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        String authorization = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) return;

        String headers = "Authorization=" + authorization + "\r\nid_token="
                + authorization.replaceFirst("(?i)^Bearer\\s+", "");

        for (StiDatabase database : report.getDictionary().getDatabases()) {
            if (database instanceof StiJsonDatabase jsonDatabase) {
                jsonDatabase.setHeadersString(headers);
            }
        }
    }

    private void fillJsonDatabases(StiReport report, Map<String, String> parameters) {
        HttpServletRequest request = currentRequest();
        String authorization = request != null ? request.getHeader(HttpHeaders.AUTHORIZATION) : null;
        String idToken = request != null ? request.getHeader("id_token") : null;

        List<StiJsonDatabase> jsonDatabases = new ArrayList<>();
        for (StiDatabase database : report.getDictionary().getDatabases()) {
            if (database instanceof StiJsonDatabase jsonDatabase) {
                jsonDatabases.add(jsonDatabase);
            }
        }

        for (StiJsonDatabase jsonDatabase : jsonDatabases) {
            String path = jsonDatabase.getPathData();
            if (path == null || path.isBlank()) {
                continue;
            }

            String url = expandPlaceholders(path, parameters);
            if (!url.startsWith("http")) {
                continue;
            }

            try {
                String json = fetchJson(url, authorization, idToken);
                String tableName = jsonDatabase.getName();
                DataTable table = toDataTable(tableName, json);

                report.regData(tableName, table);
                report.getDictionary().getDatabases().remove(jsonDatabase);
            } catch (Exception e) {
                throw new RuntimeException("Failed to bind JSON for " + jsonDatabase.getName(), e);
            }

            report.getDictionary().getDatabases().remove(jsonDatabase);
        }
    }

    private DataTable toDataTable(String tableName, String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        List<Map<String, Object>> records = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                records.add(mapper.convertValue(node, new TypeReference<Map<String, Object>>() {
                }));
            }
        } else if (root.isObject()) {
            records.add(mapper.convertValue(root, new TypeReference<Map<String, Object>>() {
            }));
        }

        DataTable table = new DataTable(tableName);
        if (records.isEmpty()) {
            return table;
        }


        for (String key : records.get(0).keySet()) {
            StiDataColumn column = new StiDataColumn();
            column.setName(key);
            column.setAlias(key);
            table.getColumns().add(column);
        }
        for (Map<String, Object> record : records) {
            DataRow row = new DataRow(table);
            table.getRows().add(row);
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                row.setValue(entry.getKey(), entry.getValue());
            }
        }
        return table;
    }

    private String expandPlaceholders(String path, Map<String, String> parameters) {
        String result = path;
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}",
                        entry.getValue() == null ? "" : entry.getValue());
            }
        }
        return result;
    }

    private String fetchJson(String url, String authorization, String idToken) {
        HttpHeaders headers = new HttpHeaders();
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        if (idToken != null && !idToken.isBlank()) {
            headers.set("id_token", idToken);
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException(
                    "Failed to load report data from " + url + " (" + response.getStatusCode() + ")");
        }
        return response.getBody();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}