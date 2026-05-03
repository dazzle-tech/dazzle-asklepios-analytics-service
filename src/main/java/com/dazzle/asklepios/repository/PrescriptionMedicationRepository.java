package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientPrescriptionMedication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionMedicationRepository extends JpaRepository<PatientPrescriptionMedication, Long> {
    List<PatientPrescriptionMedication> findAllByPrescriptionHeaderIdOrderByIdAsc(Long prescriptionHeaderId);

    List<PatientPrescriptionMedication> findAllByPrescriptionHeaderIdInOrderByIdAsc(List<Long> prescriptionHeaderIds);
}