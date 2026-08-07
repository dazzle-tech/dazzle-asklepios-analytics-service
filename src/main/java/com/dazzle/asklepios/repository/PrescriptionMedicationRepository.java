package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientPrescriptionMedication;
import com.dazzle.asklepios.domain.enumeration.PrescriptionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionMedicationRepository extends JpaRepository<PatientPrescriptionMedication, Long> {
    List<PatientPrescriptionMedication> findAllByPrescriptionHeaderIdOrderByIdAsc(Long prescriptionHeaderId);

    List<PatientPrescriptionMedication> findAllByPrescriptionHeaderIdInAndStatusNotOrderByIdAsc(List<Long> prescriptionHeaderIds , PrescriptionStatus status);
}