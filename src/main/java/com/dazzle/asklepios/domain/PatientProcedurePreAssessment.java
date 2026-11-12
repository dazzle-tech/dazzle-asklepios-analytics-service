package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Pre-procedure assessment.
 * Read-only view of a table owned by patient-service; related rows are referenced by id.
 */
@Entity
@Immutable
@Table(name = "patient_procedure_pre_assessment")
@Getter
@Setter
@NoArgsConstructor
public class PatientProcedurePreAssessment extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    private Long id;

    @Column(name = "patient_procedure_id", nullable = false)
    private Long patientProcedureId;

    @Column(name = "fasting_required")
    private Boolean fastingRequired;

    @Column(name = "patient_prepared")
    private Boolean patientPrepared;

    @Column(name = "special_instructions")
    private String specialInstructions;

    @Column(name = "blood_pressure_systolic")
    private BigDecimal bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private BigDecimal bloodPressureDiastolic;

    @Column(name = "heart_rate")
    private BigDecimal heartRate;

    @Column(name = "temperature")
    private BigDecimal temperature;

    @Column(name = "oxygen_saturation")
    private BigDecimal oxygenSaturation;

    @Column(name = "patient_identity_verified")
    private Boolean patientIdentityVerified;

    @Column(name = "consent_confirmed")
    private Boolean consentConfirmed;

    @Column(name = "procedure_site_marked")
    private Boolean procedureSiteMarked;

    @Column(name = "allergies_confirmed")
    private Boolean allergiesConfirmed;

    @Column(name = "patient_premedicated")
    private Boolean patientPremedicated;

    @Column(name = "equipment_counting_done")
    private Boolean equipmentCountingDone;

    @Column(name = "asa_score")
    private String asaScore;

    @Column(name = "airway_grade")
    private String airwayGrade;
}
