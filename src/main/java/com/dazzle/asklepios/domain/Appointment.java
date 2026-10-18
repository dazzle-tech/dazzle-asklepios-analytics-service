package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "appointment")
@Getter
@Setter
public class Appointment extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @ManyToOne
    @JoinColumn(name = "availability_generation_batch_id")
    private AvailabilityGenerationBatch availabilityGenerationBatch;


    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "capacity_index")
    private Integer capacityIndex;

    @Column(name = "start_datetime", nullable = false)
    private Instant startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private Instant endDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "default_service_id")
    private Long defaultServiceId;

    @Column(name = "default_practitioner_id")
    private Long defaultPractitionerId;

    @Column(name = "require_practitioner", nullable = false)
    private Boolean requirePractitioner = false;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "service", length = 50)
    private String service;

    @Column(name = "service_group_id")
    private Long serviceGroupId;

    @Column(name = "booking_mode", nullable = false, length = 20)
    private String bookingMode;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "deferred", nullable = false)
    private Boolean deferred = false;

    @Column(name = "deferred_at")
    private Instant deferredAt;

    @Column(name = "require_confirmation", nullable = false)
    private Boolean requireConfirmation = true;

    @Column(name = "no_show_reason", length = 255)
    private String noShowReason;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "cancelled_by", length = 50)
    private String cancelledBy;

    @Column(name = "priority", nullable = false, length = 50)
    private String priority;

    @Column(name = "origin_type", length = 255)
    private String originType;

    @Column(name = "origin_name", length = 255)
    private String originName;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_up_encounter_id")
    private PatientEncounter followUpEncounter;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_group_id")
    private AppointmentBookingGroup bookingGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiting_list_id")
    private AppointmentWaitingList waitingList;

    @Column(name="hl7_appointment_number", length = 50)
    private String hl7AppointmentNumber;

}
