package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Table(name = "icd_diagnosis")
public class ICDDiagnosis implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "icd_diagnosis_uid", nullable = false, length = 100, unique = true)
    private String icdDiagnosisUid;

    @NotNull
    @Column(name = "icd_code", nullable = false, length = 50)
    private String icdCode;

    @NotNull
    @Column(name = "icd_coding", nullable = false, length = 10)
    private String icdCoding;

    @NotNull
    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "icd_short_description", length = 500)
    private String icdShortDescription;

    @Column(name = "icd_full_description", columnDefinition = "text")
    private String icdFullDescription;
}
