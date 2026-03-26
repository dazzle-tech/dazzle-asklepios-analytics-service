package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "brand_medication")
public class BrandMedication extends AbstractAuditingEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(length = 50)
    private String manufacturer;

    @Column(name = "dosage_form", nullable = false, length = 50)
    private String dosageForm;

    @Column(name = "usage_instructions", columnDefinition = "text")
    private String usageInstructions;

    @Column(name = "storage_requirements", columnDefinition = "text")
    private String storageRequirements;

    @Column(name = "expires_after_opening")
    private Boolean expiresAfterOpening;

    @Column(name = "expires_after_opening_value", precision = 10, scale = 3)
    private BigDecimal expiresAfterOpeningValue;


    @Column(name = "single_patient_use")
    private Boolean useSinglePatient;

    @Column(name = "high_cost_medication")
    private Boolean highCostMedication;

    @Column(name = "cost_category", length = 50)
    private String costCategory;

    @Column(length = 200)
    private String roa;

    @Column(name = "is_active")
    private Boolean isActive = true;


}
