package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UrgentCareMedicationOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UrgentCareMedicationOrderRepository extends JpaRepository<UrgentCareMedicationOrder, Long> {

    @EntityGraph(attributePaths = {"activeIngredient"})
    List<UrgentCareMedicationOrder> findByEncounterIdAndStatusOrderByCreatedDateAsc(
            Long encounterId,
            String status
    );
}
