package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrderTestCollectedSample;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiagnosticOrderTestCollectedSampleRepository
        extends JpaRepository<DiagnosticOrderTestCollectedSample, Long> {

    Optional<DiagnosticOrderTestCollectedSample>
    findTopByOrderTestIdOrderByCreatedDateDescIdDesc(Long orderTestId);
    List<DiagnosticOrderTestCollectedSample> findAllByOrderTestIdOrderByCreatedDateDescIdDesc(Long orderTestId);
    boolean existsByOrderTestId(Long orderTestId);
}
