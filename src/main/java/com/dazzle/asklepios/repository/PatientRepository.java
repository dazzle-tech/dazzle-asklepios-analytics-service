package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PatientRepository extends JpaRepository<Patient, Long>,
        JpaSpecificationExecutor<Patient> {
}

