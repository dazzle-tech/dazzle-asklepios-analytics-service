package com.dazzle.asklepios.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Post-procedure care, including the Aldrete score parts and discharge criteria.
 * Read-only view of a table owned by patient-service; related rows are referenced by id.
 */
@Entity
@Immutable
@Table(name = "patient_procedure_post_care")
@Getter
@Setter
@NoArgsConstructor
public class PatientProcedurePostCare extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    private Long id;

    @Column(name = "patient_procedure_id", nullable = false)
    private Long patientProcedureId;

    @Column(name = "patient_condition")
    private String patientCondition;

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

    @Column(name = "pain_assessment_type")
    private String painAssessmentType;

    @Column(name = "pain_score")
    private String painScore;

    @Column(name = "pain_description")
    private String painDescription;

    @Column(name = "recovery_notes")
    private String recoveryNotes;

    @Column(name = "additional_observations")
    private String additionalObservations;

    @Column(name = "equipment_count_done")
    private Boolean equipmentCountDone;

    @Column(name = "count_status")
    private String countStatus;

    @Column(name = "nausea_vomiting")
    private Boolean nauseaVomiting;

    @Column(name = "awake_and_oriented")
    private Boolean awakeAndOriented;

    @Column(name = "tolerating_oral_fluids")
    private Boolean toleratingOralFluids;

    @Column(name = "ambulating_independently")
    private Boolean ambulatingIndependently;

    @Column(name = "voided_urine")
    private Boolean voidedUrine;

    @Column(name = "no_active_bleeding")
    private Boolean noActiveBleeding;

    @Column(name = "pain_score_4")
    private Boolean painScore4;

    @Column(name = "activity")
    private String activity;

    @Column(name = "respiration")
    private String respiration;

    @Column(name = "circulation")
    private String circulation;

    @Column(name = "consciousness")
    private String consciousness;

    @Column(name = "oxygen_saturation_level")
    private String oxygenSaturationLevel;

    @Column(name = "aldrete_score")
    private BigDecimal aldreteScore;

    /** ICD diagnosis ids, loaded with a separate select when read. */
    @ElementCollection
    @CollectionTable(name = "patient_procedure_post_care_diagnosis", joinColumns = @JoinColumn(name = "post_care_id"))
    @Column(name = "diagnosis_icd_id")
    private Set<Long> diagnosisIcdIds = new HashSet<>();
}
