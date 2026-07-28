package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticOrderRepository extends JpaRepository<DiagnosticOrder, Long> {

    List<DiagnosticOrder>  findByEncounterIdOrderByCreatedDateAsc(Long encounterId);

    List<DiagnosticOrder>  findByOrderNumber(Long orderNumber);

    List<DiagnosticOrder> findByPatientIdOrderByCreatedDateDesc(Long patientId);
    List<DiagnosticOrder> findByPatientId(Long patientId);
}