package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CurrentMedication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrentMedicationRepository extends JpaRepository<CurrentMedication, Long> {

    @EntityGraph(attributePaths = "activeIngredient")
    List<CurrentMedication> findByPatientIdAndStatusOrderByCreatedDateAsc(
            Long patientId,
            String status
    );
}