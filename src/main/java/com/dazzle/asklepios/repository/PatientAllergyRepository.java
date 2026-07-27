package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.enumeration.PatientAllergyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientAllergyRepository extends JpaRepository<PatientAllergies, Long> {
    List<PatientAllergies> findAllByPatientId(Long patientId);

}