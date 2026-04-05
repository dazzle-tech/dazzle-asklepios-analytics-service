package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientPrescriptionMedication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientPrescriptionMedicationRepository
        extends JpaRepository<PatientPrescriptionMedication, Long> {

    @EntityGraph(attributePaths = {"medications"})
    List<PatientPrescriptionMedication> findByPrescriptionHeader_IdOrderByIdAsc(
            Long prescriptionHeaderId);
}