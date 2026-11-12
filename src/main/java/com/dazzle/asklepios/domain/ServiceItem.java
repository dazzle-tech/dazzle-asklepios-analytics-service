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
 * Read-only name lookup for the setup {@code service} table (billable services).
 */
@Entity
@Immutable
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
public class ServiceItem implements Serializable {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;
}
