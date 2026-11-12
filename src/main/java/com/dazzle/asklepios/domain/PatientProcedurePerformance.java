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
import java.time.Instant;

/**
 * How the procedure was performed.
 * Read-only view of a table owned by patient-service; related rows are referenced by id.
 */
@Entity
@Immutable
@Table(name = "patient_procedure_performance")
@Getter
@Setter
@NoArgsConstructor
public class PatientProcedurePerformance extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    private Long id;

    @Column(name = "patient_procedure_id", nullable = false)
    private Long patientProcedureId;

    @Column(name = "actual_start_time")
    private Instant actualStartTime;

    @Column(name = "actual_end_time")
    private Instant actualEndTime;

    @Column(name = "anesthesia_used")
    private Boolean anesthesiaUsed;

    @Column(name = "anesthesia_type")
    private String anesthesiaType;

    @Column(name = "anesthesia_start_time")
    private Instant anesthesiaStartTime;

    @Column(name = "anesthesia_end_time")
    private Instant anesthesiaEndTime;

    @Column(name = "anesthesia_administered_by_id")
    private Long anesthesiaAdministeredById;

    @Column(name = "time_out")
    private Boolean timeOut;

    @Column(name = "procedure_outcome")
    private String procedureOutcome;

    @Column(name = "observations")
    private String observations;

    @Column(name = "additional_notes")
    private String additionalNotes;

    @Column(name = "home_instruction")
    private String homeInstruction;

    @Column(name = "home_instruction_notes")
    private String homeInstructionNotes;
}
