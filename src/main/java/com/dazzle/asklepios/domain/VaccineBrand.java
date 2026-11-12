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
 * Read-only name lookup for the setup {@code vaccine_brands} table.
 */
@Entity
@Immutable
@Table(name = "vaccine_brands")
@Getter
@Setter
@NoArgsConstructor
public class VaccineBrand implements Serializable {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;
}
