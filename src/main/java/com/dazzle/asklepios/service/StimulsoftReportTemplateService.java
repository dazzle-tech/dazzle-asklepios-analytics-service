package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.StimulsoftReportTemplate;
import com.dazzle.asklepios.domain.enumeration.StimulsoftTemplateType;
import com.dazzle.asklepios.repository.StimulsoftReportTemplateRepository;
import com.dazzle.asklepios.service.dto.reportTemplate.StimulsoftReportTemplateWriteDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.report.StimulsoftReportTemplateVM;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StimulsoftReportTemplateService {

    private final StimulsoftReportTemplateRepository repository;

    public StimulsoftReportTemplateService(StimulsoftReportTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<StimulsoftReportTemplateVM> findAll(StimulsoftTemplateType templateType, Pageable pageable) {
        Page<StimulsoftReportTemplate> page = templateType == null
                ? repository.findAll(pageable)
                : repository.findByTemplateType(templateType, pageable);
        return page.map(this::toVM);
    }

    @Transactional(readOnly = true)
    public Page<StimulsoftReportTemplateVM> findByName(
            String name,
            StimulsoftTemplateType templateType,
            Pageable pageable
    ) {
        Page<StimulsoftReportTemplate> page = templateType == null
                ? repository.findByNameContainingIgnoreCase(name, pageable)
                : repository.findByNameContainingIgnoreCaseAndTemplateType(name, templateType, pageable);
        return page.map(this::toVM);
    }

    @Transactional(readOnly = true)
    public StimulsoftReportTemplateVM findById(Long id) {

        return repository.findById(id)
                .map(this::toVM)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Stimulsoft report template not found: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public StimulsoftReportTemplate findEntityById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Stimulsoft report template not found: " + id
                        )
                );
    }

    public StimulsoftReportTemplateVM create(StimulsoftReportTemplateWriteDTO request) {

        if (repository.existsByCode(request.code())) {
            throw new BadRequestAlertException("Report template code already exists: " + request.code(), "reportTemplate", "report.code.exist");
        }

        StimulsoftReportTemplate entity = new StimulsoftReportTemplate();

        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setTemplateJson(request.templateJson());
        entity.setActive(
                request.isActive() == null
                        ? true
                        : request.isActive()
        );
        entity.setFacilityId(request.facilityId());
        entity.setDepartmentIds(request.departmentIds());
        entity.setModule(request.module());
        entity.setTemplateType(
                request.templateType() == null
                        ? StimulsoftTemplateType.REPORT
                        : request.templateType()
        );

        return toVM(repository.save(entity));
    }

    public StimulsoftReportTemplateVM update(Long id, StimulsoftReportTemplateWriteDTO request) {

        StimulsoftReportTemplate entity = findEntityById(id);

        if (!entity.getCode().equals(request.code()) && repository.existsByCodeAndIdNot(request.code(), id)) {
            throw new BadRequestAlertException("Report template code already exists: " + request.code(), "reportTemplate", "report.code.exist");
        }

        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setTemplateJson(request.templateJson());

        if (request.isActive() != null) {
            entity.setActive(request.isActive());
        }

        entity.setFacilityId(request.facilityId());
        entity.setDepartmentIds(request.departmentIds());
        entity.setModule(request.module());
        if (request.templateType() != null) {
            entity.setTemplateType(request.templateType());
        }

        return toVM(repository.save(entity));
    }

    public StimulsoftReportTemplateVM toggleActive(Long id) {

        StimulsoftReportTemplate entity = findEntityById(id);

        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));

        return toVM(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public String getTemplateContent(String code) {

        StimulsoftReportTemplate template = repository
                .findByCode(code)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Stimulsoft report template not found with code: " + code
                        )
                );

        if (!Boolean.TRUE.equals(template.getActive())) {
            throw new BadRequestAlertException(
                    "Stimulsoft report template is inactive: " + code,
                    "reportTemplate",
                    "report.template.inactive"
            );
        }

        if (template.getTemplateJson() == null
                || template.getTemplateJson().isBlank()) {

            throw new BadRequestAlertException(
                    "Stimulsoft report template has no template content: " + code,
                    "reportTemplate",
                    "report.template.empty"
            );
        }

        return template.getTemplateJson();
    }

    private StimulsoftReportTemplateVM toVM(StimulsoftReportTemplate entity) {

        return new StimulsoftReportTemplateVM(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getTemplateJson(),
                entity.getActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate(),
                entity.getFacilityId(),
                entity.getDepartmentIds(),
                entity.getModule(),
                entity.getTemplateType() == null
                        ? StimulsoftTemplateType.REPORT
                        : entity.getTemplateType()
        );
    }
}