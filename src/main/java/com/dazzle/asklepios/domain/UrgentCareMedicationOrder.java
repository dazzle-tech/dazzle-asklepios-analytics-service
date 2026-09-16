package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.MedicationOrderStatus;
import com.dazzle.asklepios.domain.enumeration.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "urgent_care_medication_order")
@EqualsAndHashCode(callSuper = false)
public class UrgentCareMedicationOrder extends AbstractAuditingEntity<Long>
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id", nullable = false)
    private PatientEncounter encounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_ingredient_id", nullable = false)
    private ActiveIngredients activeIngredient;

    @Column(name = "dose")
    private Long dose;

    @Column(name = "dose_unit", length = 100)
    private String doseUnit;

    @Column(name = "frequency_number")
    private Integer frequencyNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_unit", length = 20)
    private Unit frequencyUnit;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "dose_time")
    private LocalDateTime doseTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MedicationOrderStatus status;
}
