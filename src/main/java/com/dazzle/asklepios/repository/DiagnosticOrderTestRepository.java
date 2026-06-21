package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticOrderTestRepository extends JpaRepository<DiagnosticOrderTest, Long> {
    List<DiagnosticOrderTest> findByOrderIdInOrderByIdAsc(List<Long> orderIds);
    List<DiagnosticOrderTest> findByOrderIdIn(List<Long> orderIds);
}