package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Facility extends AbstractAuditingEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(nullable = false, length = 50)
    private String type;

    @NotNull
    @Column(nullable = false, length = 50)
    private String code;


    @Column(length = 100)
    private String emailAddress;

    @Column(length = 100)
    private String phone1;

    @Column(length = 100)
    private String phone2;

    @Column(length = 100)
    private String fax;

    @Column(length = 100)
    private String addressId;

    @NotNull
    @Column(nullable = false, length = 10)
    private String defaultCurrency;

    @NotNull
    @Column(nullable = false, length = 10)
    private Boolean isActive = true;

    @Column(name = "rule_id")
    private Long ruleId;

}
