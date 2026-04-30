package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientPreferredHealthProfessional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientPreferredHealthProfessionalRepository
        extends JpaRepository<PatientPreferredHealthProfessional, Long> {
    Optional<PatientPreferredHealthProfessional> findFirstByPatientId(Long patientId);
}
