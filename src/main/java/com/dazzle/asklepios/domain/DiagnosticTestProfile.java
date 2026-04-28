package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.TestResultType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diagnostic_test_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticTestProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private DiagnosticTest test;

    @NotNull(message = "Name cannot be null")
    @Column(nullable = false)
    private String name;

    private String resultUnit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false)
    private TestResultType resultType;

    @NotNull
    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "list_of_value_id")
    private String listOfValueId;

    @Column(name ="is_active",nullable = false)
    private Boolean isActive = true;

}

