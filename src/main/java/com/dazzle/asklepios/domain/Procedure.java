package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "procedure")
public class Procedure extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(nullable = false, length = 255)
    private String code;

    @NotNull
    @Column(nullable = false)
    private String currency;

    @NotNull
    @Column(nullable = false)
    private  Long price;

    @NotNull
    @Column(name = "category_type", nullable = false, length = 255)
    private String categoryType;

    @NotNull
    @Column(name = "is_appointable", nullable = false)
    private Boolean isAppointable;



    @Column(name = "indications", columnDefinition = "text")
    private String indications;


    @Column(name = "contraindications", columnDefinition = "text")
    private String contraindications;


    @Column(name = "preparation_instructions", columnDefinition = "text")
    private String preparationInstructions;

    @Column(name = "recovery_notes", columnDefinition = "text")
    private String recoveryNotes;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;
}
