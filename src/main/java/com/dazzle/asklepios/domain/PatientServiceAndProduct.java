package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.PatientServiceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "patient_services_and_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientServiceAndProduct extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "encounter_id", nullable = false)
    private Long encounterId;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "brand_medication_id")
    private Long brandMedicationId;

    @Column(name = "diagnostic_test_id")
    private Long diagnosticTestId;

    @Column(name = "procedure_id")
    private Long procedureId;

    @Column(name = "vaccine_brand_id")
    private Long vaccineBrandId;

    @Column(name = "billing_item_type", length = 50)
    private String billingItemType;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "notes")
    private String notes;

    @Column(name = "quantity", nullable = false)
    private Long quantity = 1L;

}

