package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.JobRole;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Practitioner extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    @NotNull(message = "Facility cannot be null")
    private Facility facility;

    @NotNull
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotNull
    @Column(name = "last_name", nullable = false)
    private String lastName;


    @Email
    @Column
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false)
    private Specialty specialty;

    @Column(name = "sub_specialty")
    private String subSpecialty;

    @Column(name = "default_medical_license")
    private String defaultMedicalLicense;

    @Column(name = "secondary_medical_license")
    private String secondaryMedicalLicense;

    @Column(name = "educational_level")
    private String educationalLevel;


    @Column
    private Boolean appointable;


    @OneToOne(optional = true)
    @JoinColumn(name = "user_id", nullable = true )
    private User user;

    @Column(name = "default_license_valid_until")
    private LocalDate defaultLicenseValidUntil;

    @Column(name = "secondary_license_valid_until")
    private LocalDate secondaryLicenseValidUntil;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "job_role")
    private JobRole jobRole;

    @Column
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "is_active")
    private Boolean isActive = true;


}
