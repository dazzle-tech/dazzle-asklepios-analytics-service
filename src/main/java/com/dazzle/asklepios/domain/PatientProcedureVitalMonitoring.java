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
import java.time.Instant;

/**
 * One intra-procedure vital signs reading.
 * Read-only view of a table owned by patient-service; related rows are referenced by id.
 */
@Entity
@Immutable
@Table(name = "patient_procedure_vital_monitoring")
@Getter
@Setter
@NoArgsConstructor
public class PatientProcedureVitalMonitoring extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    private Long id;

    @Column(name = "patient_procedure_id", nullable = false)
    private Long patientProcedureId;

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @Column(name = "heart_rate")
    private BigDecimal heartRate;

    @Column(name = "oxygen_saturation")
    private BigDecimal oxygenSaturation;

    @Column(name = "blood_pressure_systolic")
    private BigDecimal bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private BigDecimal bloodPressureDiastolic;

    @Column(name = "respiratory_rate")
    private BigDecimal respiratoryRate;

    @Column(name = "temperature")
    private BigDecimal temperature;

    @Column(name = "etco2")
    private BigDecimal etco2;

    @Column(name = "fluids_given")
    private BigDecimal fluidsGiven;

    @Column(name = "urine_output")
    private BigDecimal urineOutput;
}
