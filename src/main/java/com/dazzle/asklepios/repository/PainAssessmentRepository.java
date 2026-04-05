package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PainAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PainAssessmentRepository extends JpaRepository<PainAssessment, Long> {

    Optional<PainAssessment> findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(Long encounterId);

    Optional<PainAssessment> findFirstByEncounterIdAndIsActiveTrueAndCreatedDateBetweenOrderByCreatedDateDesc(
            Long encounterId,
            Instant dayStart,
            Instant dayEnd
    );

}
