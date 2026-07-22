package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.enumeration.DiagnosticOrderTestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticOrderTestRepository extends JpaRepository<DiagnosticOrderTest, Long> {
    List<DiagnosticOrderTest> findByOrderIdOrderByIdAsc(Long orderId);
    List<DiagnosticOrderTest> findByOrderIdInAndStatusNotOrderByIdAsc(
            List<Long> orderIds,
            DiagnosticOrderTestStatus status
    );
    List<DiagnosticOrderTest> findByOrderIdIn(List<Long> orderIds);

}