package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.PayorCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "payor")

public class Payor extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Payor code cannot be null")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull(message = "Payor name cannot be null")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull(message = "Category cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private PayorCategory category;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "contract_manager_contact", length = 255)
    private String contractManagerContact;

    // Contract Period
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @NotNull
    @Column(name = "renewable", nullable = false)
    private Boolean renewable = false;

    // Rules
    @NotNull
    @Column(name = "allow_partial_coverage", nullable = false)
    private Boolean allowPartialCoverage = false;

    @NotNull
    @Column(name = "accept_copay", nullable = false)
    private Boolean acceptCopay = false;

    @NotNull
    @Column(name = "accept_deductibles", nullable = false)
    private Boolean acceptDeductibles = false;

    @NotNull
    @Column(name = "allow_package_pricing", nullable = false)
    private Boolean allowPackagePricing = false;

    @NotNull
    @Column(name = "allow_drg_billing", nullable = false)
    private Boolean allowDrgBilling = false;

    @NotNull
    @Column(name = "force_preapproval", nullable = false)
    private Boolean forcePreApproval = false;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
