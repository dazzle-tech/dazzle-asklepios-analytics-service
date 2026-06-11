package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diagnostic_test_laboratory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticTestLaboratory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false, unique = true)
    private DiagnosticTest test;


    @Column(name = "test_duration_time")
    private Double testDurationTime;

    @Column(length = 200)
    private String timeUnit;

    @Column(length = 200)
    private String sampleContainer;

    private Double sampleVolume;

    @Column(length = 200)
    private String sampleVolumeUnit;

    @Column(length = 200)
    private String tubeColor;

    @Column(length = 1000)
    private String testDescription;

    @Column(length = 1000)
    private String sampleHandling;

    private Double turnaroundTime;

    @Column(length = 200)
    private String turnaroundTimeUnit;

    @Column(length = 1000)
    private String preparationRequirements;

    @Column(length = 1000)
    private String medicalIndications;

    @Column(length = 1000)
    private String associatedRisks;

    @Column(length = 1000)
    private String testInstructions;

    @Column(length = 200, nullable = false)
    private String category;

    @Column(length = 200)
    private String tubeType;
}
