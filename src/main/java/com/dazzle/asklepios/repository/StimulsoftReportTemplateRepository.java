package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.StimulsoftReportTemplate;
import com.dazzle.asklepios.domain.enumeration.StimulsoftTemplateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StimulsoftReportTemplateRepository extends JpaRepository<StimulsoftReportTemplate, Long> {

    Page<StimulsoftReportTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<StimulsoftReportTemplate> findByTemplateType(StimulsoftTemplateType templateType, Pageable pageable);

    Page<StimulsoftReportTemplate> findByNameContainingIgnoreCaseAndTemplateType(
            String name,
            StimulsoftTemplateType templateType,
            Pageable pageable
    );

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<StimulsoftReportTemplate> findByCode(String code);

}
