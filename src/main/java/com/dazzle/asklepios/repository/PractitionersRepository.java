package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PractitionersRepository extends JpaRepository<Practitioner, Long> {

    Page<Practitioner> findAll(Pageable pageable);

    Page<Practitioner> findByFacilityId(Long facilityId, Pageable pageable);

    Page<Practitioner> findBySpecialty(Specialty specialty, Pageable pageable);
    Page<Practitioner> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );
    boolean existsByUserId(Long userId);

    Page<Practitioner> findBySubSpecialtyAndIsActiveTrue(String specialty, Pageable pageable);
    Page<Practitioner> findByIsActiveTrueAndAppointableTrue(
    Pageable pageable
    );
    Optional<Practitioner> findByUserId(Long userId);

    Page<Practitioner> findByFacilityIdAndSubSpecialtyAndSpecialtyAndUserIdIsNotNullAndIsActiveTrue(
            Long facilityId,
            String subSpecialty,
            Specialty specialty,
            Pageable pageable
    );

    Page<Practitioner> findByIdInAndIsActiveTrueAndAppointableTrue(
            List<Long> ids,
            Pageable pageable
    );

}
