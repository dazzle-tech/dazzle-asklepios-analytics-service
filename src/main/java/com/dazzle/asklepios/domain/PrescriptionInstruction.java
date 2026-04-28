package com.dazzle.asklepios.domain;


import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.MedRoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PrescriptionInstruction implements Serializable {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private AgeGroupType category;

    @NotNull
    @Column(name = "dose")
    private BigDecimal dose;

    @NotNull
    @Column(name = "unit")
    private String unit;

    @NotNull
    @Column(name = "rout")
    private String rout;

    @NotNull
    @Column(name = "frequency")
    private String frequency;

}
