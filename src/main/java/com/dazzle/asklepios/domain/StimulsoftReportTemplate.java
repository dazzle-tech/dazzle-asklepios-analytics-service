package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Modules;
import com.dazzle.asklepios.domain.enumeration.StimulsoftTemplateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stimulsoft_report_template", schema = "reports")
public class StimulsoftReportTemplate extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name="department_ids", columnDefinition = "text")
    private String departmentIds;

    @Column(name = "module", length = 50)
    private Modules module;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 20)
    private StimulsoftTemplateType templateType = StimulsoftTemplateType.REPORT;

    /**
     * Stimulsoft MRT JSON.
     */
    @Lob
    @Column(name = "template_json", columnDefinition = "text")
    private String templateJson;

    @Column(nullable = false)
    private Boolean active = true;

}
