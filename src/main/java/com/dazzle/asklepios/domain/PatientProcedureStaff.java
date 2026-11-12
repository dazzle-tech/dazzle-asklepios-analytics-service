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
 * A team member on a procedure.
 * Read-only view of a table owned by patient-service; related rows are referenced by id.
 */
@Entity
@Immutable
@Table(name = "patient_procedure_staff")
@Getter
@Setter
@NoArgsConstructor
public class PatientProcedureStaff extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    private Long id;

    @Column(name = "patient_procedure_id", nullable = false)
    private Long patientProcedureId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "responsibility")
    private String responsibility;

    @Column(name = "is_present")
    private Boolean isPresent;
}
