package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {

    Optional<VitalSigns> findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(Long encounterId);

}
