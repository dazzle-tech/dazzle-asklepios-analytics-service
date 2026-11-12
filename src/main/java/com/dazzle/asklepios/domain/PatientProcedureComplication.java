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

/**
 * A complication recorded during the procedure.
 * Read-only view of a table owned by patient-service; related rows are referenced by id.
 */
@Entity
@Immutable
@Table(name = "patient_procedure_complication")
@Getter
@Setter
@NoArgsConstructor
public class PatientProcedureComplication extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    private Long id;

    @Column(name = "patient_procedure_id", nullable = false)
    private Long patientProcedureId;

    @Column(name = "complication_type")
    private String complicationType;

    @Column(name = "severity")
    private String severity;

    @Column(name = "actions_taken")
    private String actionsTaken;
}
