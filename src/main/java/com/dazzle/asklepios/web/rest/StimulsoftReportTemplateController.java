package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.JobRole;
import com.dazzle.asklepios.domain.enumeration.StimulsoftTemplateType;
import com.dazzle.asklepios.service.StimulsoftDesignerSchemaService;
import com.dazzle.asklepios.service.StimulsoftReportTemplateService;
import com.dazzle.asklepios.service.dto.reportTemplate.StimulsoftReportTemplateWriteDTO;
import com.dazzle.asklepios.web.rest.vm.report.DesignerSchemaVM;
import com.dazzle.asklepios.web.rest.vm.report.StimulsoftReportTemplateVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/analytics")
public class StimulsoftReportTemplateController {

    private final StimulsoftReportTemplateService service;
    private final StimulsoftDesignerSchemaService stimulsoftDesignerSchemaService;

    public StimulsoftReportTemplateController(StimulsoftReportTemplateService service, StimulsoftDesignerSchemaService stimulsoftDesignerSchemaService) {
        this.service = service;
        this.stimulsoftDesignerSchemaService = stimulsoftDesignerSchemaService;
    }

    @GetMapping("/reports/templates")
    public ResponseEntity<Page<StimulsoftReportTemplateVM>> getAll(
            @RequestParam(required = false) StimulsoftTemplateType templateType,
            @PageableDefault(size = 20) Pageable pageable
    ) {

        return ResponseEntity.ok(
                service.findAll(templateType, pageable)
        );
    }

    /**
     * Dashboards visible to one user. The designer continues to use
     * {@code GET /reports/templates}, which returns every declared dashboard.
     */
    @GetMapping("/reports/dashboards")
    public ResponseEntity<Page<StimulsoftReportTemplateVM>> getViewableDashboards(
            @RequestParam(required = false) JobRole jobRole,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 200, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(
                service.findViewableDashboards(jobRole, userId, facilityId, departmentId, pageable)
        );
    }

    @GetMapping("/reports/templates/by-name/{name}")
    public ResponseEntity<Page<StimulsoftReportTemplateVM>> getByName(
            @PathVariable String name,
            @RequestParam(required = false) StimulsoftTemplateType templateType,
            @PageableDefault(size = 20) Pageable pageable
    ) {

        return ResponseEntity.ok(
                service.findByName(name, templateType, pageable)
        );
    }

    @GetMapping("/reports/templates/{id}")
    public ResponseEntity<StimulsoftReportTemplateVM> getById(@PathVariable Long id) {

        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @PostMapping("/reports/templates")
    public ResponseEntity<StimulsoftReportTemplateVM> create(@RequestBody StimulsoftReportTemplateWriteDTO request) {

        StimulsoftReportTemplateVM response =
                service.create(request);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/reports/templates/"
                                        + response.id()
                        )
                )
                .body(response);
    }

    @PutMapping("/reports/templates/{id}")
    public ResponseEntity<StimulsoftReportTemplateVM> update(@PathVariable Long id, @RequestBody StimulsoftReportTemplateWriteDTO request) {

        return ResponseEntity.ok(
                service.update(id, request)
        );
    }

    @PatchMapping("/reports/templates/{id}/toggle-active")
    public ResponseEntity<StimulsoftReportTemplateVM> toggleActive(@PathVariable Long id) {

        return ResponseEntity.ok(
                service.toggleActive(id)
        );
    }

    @GetMapping("/reports/templates/{idOrCode}/schema")
    public ResponseEntity<DesignerSchemaVM> getSchema(
            @PathVariable String idOrCode
    ) {

        return ResponseEntity.ok(
                stimulsoftDesignerSchemaService.getSchema()
        );
    }
}