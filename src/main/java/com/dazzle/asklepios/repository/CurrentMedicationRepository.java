package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CurrentMedication;
import com.dazzle.asklepios.domain.enumeration.PatientHistoryStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurrentMedicationRepository extends JpaRepository<CurrentMedication, Long> {

    @EntityGraph(attributePaths = "activeIngredient")
    List<CurrentMedication> findByPatientIdAndStatusOrderByCreatedDateAsc(
            Long patientId,
            PatientHistoryStatus status
    );
    List<CurrentMedication> findByPatientId(Long patientId);
}