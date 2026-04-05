package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientServiceAndProduct;
import io.micrometer.core.instrument.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientServiceAndProductRepository extends JpaRepository<PatientServiceAndProduct, Long> {

    Page<PatientServiceAndProduct> findAllByEncounterId(Long encounterId, Pageable pageable);

    List<PatientServiceAndProduct> findByEncounterIdOrderByCreatedDateAsc(Long encounterId);
}

