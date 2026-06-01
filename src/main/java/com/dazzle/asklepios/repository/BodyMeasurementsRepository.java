package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BodyMeasurements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BodyMeasurementsRepository extends JpaRepository<BodyMeasurements, Long> {

    Optional<BodyMeasurements> findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(Long encounterId);
    Optional<BodyMeasurements> findFirstByPatientIdAndIsActiveTrueOrderByCreatedDateDesc(Long patientId);

}
