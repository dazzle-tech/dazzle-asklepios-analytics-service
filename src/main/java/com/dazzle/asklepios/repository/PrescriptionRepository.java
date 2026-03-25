package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientPrescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<PatientPrescription, Long> {
}