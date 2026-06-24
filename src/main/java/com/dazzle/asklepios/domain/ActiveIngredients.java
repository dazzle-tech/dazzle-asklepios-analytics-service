package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "active_ingredients")
public class ActiveIngredients extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @Column(name = "atc_code", length = 100)
    private String atcCode;

    private Boolean otc;

    @Column(name = "has_synonyms")
    private Boolean hasSynonyms;

    private Boolean antimicrobial;

    @Column(name = "high_alert")
    private Boolean highAlert = false;

    @Column(name = "is_look-alike-sound-alike")
    private Boolean isLookAlikeSoundAlike = false;

    @Column(name = "abortive_medication")
    private Boolean abortiveMedication;

    @Column(name = "labor_inducing_med")
    private Boolean laborInducingMed;

    @Column(name = "is_controlled")
    private Boolean isControlled;

    @Column(name = "has_black_box_warning")
    private Boolean hasBlackBoxWarning;

    @Column(name = "black_box_warning")
    private String blackBoxWarning;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "toxicity_maximum_dose", length = 100)
    private String toxicityMaximumDose;

    @Column(name = "toxicity_maximum_dose_per_unit", length = 50)
    private String toxicityMaximumDosePerUnit;

    @Column(name = "toxicity_details", columnDefinition = "text")
    private String toxicityDetails;

    @Column(name = "mechanism_of_action", columnDefinition = "text")
    private String mechanismOfAction;

    @Column(name = "pharma_absorption", columnDefinition = "text")
    private String pharmaAbsorption;

    @Column(name = "pharma_route_of_elimination", length = 500)
    private String pharmaRouteOfElimination;

    @Column(name = "pharma_volume_of_distribution", columnDefinition = "text")
    private String pharmaVolumeOfDistribution;

    @Column(name = "pharma_half_life", columnDefinition = "text")
    private String pharmaHalfLife;

    @Column(name = "pharma_protein_binding", columnDefinition = "text")
    private String pharmaProteinBinding;

    @Column(name = "pharma_clearance", columnDefinition = "text")
    private String pharmaClearance;

    @Column(name = "pharma_metabolism", columnDefinition = "text")
    private String pharmaMetabolism;

    @Column(name = "pregnancy_category", length = 50)
    private String pregnancyCategory;

    @Column(name = "pregnancy_notes", columnDefinition = "text")
    private String pregnancyNotes;

    @Column(name = "lactation_risk", length = 50)
    private String lactationRisk;

    @Column(name = "lactation_risk_notes", columnDefinition = "text")
    private String lactationRiskNotes;

    @Column(name = "dose_adjustment_renal")
    private Boolean doseAdjustmentRenal;

    @Column(name = "dose_adj_renal_one", length = 100)
    private String doseAdjustmentRenalOne;

    @Column(name = "dose_adj_renal_two", length = 100)
    private String doseAdjustmentRenalTwo;

    @Column(name = "dose_adj_renal_three", length = 100)
    private String doseAdjustmentRenalThree;

    @Column(name = "dose_adj_renal_four", length = 100)
    private String doseAdjustmentRenalFour;

    @Column(name = "dose_adjustment_hepatic")
    private Boolean doseAdjustmentHepatic;

    @Column(name = "dose_adj_pug_a", length = 100)
    private String doseAdjustmentPugA;

    @Column(name = "dose_adj_pug_b", length = 100)
    private String doseAdjustmentPugB;

    @Column(name = "dose_adj_pug_c", length = 100)
    private String doseAdjustmentPugC;

}
