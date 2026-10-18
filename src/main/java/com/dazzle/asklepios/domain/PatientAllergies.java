package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "patient_allergies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientAllergies extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "encounter_id", nullable = false)
    private Long encounterId;

    @Column(name = "allergen_type", nullable = false)
    private String allergenType;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergen_id")
    private Allergens allergen;

    @Column(name = "allergen_name")
    private String allergenName;

    @Column(name = "severity", nullable = false)
    private String severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_class_id")
    private MedicationCategoriesClass medicationClass;

    @Column(name = "criticality")
    private String criticality;

    @Column(name = "certainty")
    private String certainty;

    @Column(name = "treatment_strategy")
    private String treatmentStrategy;

    @Column(name = "onset")
    private String onset;

    @Column(name = "onset_date_undefined")
    private boolean onsetDateUndefined = true;

    @Column(name = "onset_date")
    private Instant onsetDate;

    @Column(name = "type_of_propensity")
    private String typeOfPropensity;

    @Column(name = "by_patient")
    private boolean byPatient = true;

    @Column(name = "source_of_information")
    private String sourceOfInformation;

    @Column(name = "note")
    private String note;

    @Column(name = "allergic_reactions")
    private String allergicReactions;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolved_date")
    private Instant resolvedDate;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    @Column(name = "cancelled_date")
    private Instant cancelledDate;

    @Column(name = "cancellation_reason")
    private String cancellationReason;


}
