package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Pharmaceutical prescribed by the doctor (managed by setup-service), e.g. "LORNOXICAM - 8 MG VIAL".
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "pharmaceutical")
public class Pharmaceutical extends AbstractAuditingEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pharmaceutical_name", nullable = false, length = 255)
    private String pharmaceuticalName;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "billing_code", length = 50)
    private String billingCode;

    @Column(name = "second_description", length = 255)
    private String secondDescription;
}
