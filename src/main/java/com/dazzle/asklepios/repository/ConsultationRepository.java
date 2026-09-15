package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Consultation;
import com.dazzle.asklepios.domain.enumeration.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByEncounterIdAndStatusNotOrderByCreatedDateAsc(
            Long encounterId,
            ConsultationStatus status
    );
}
