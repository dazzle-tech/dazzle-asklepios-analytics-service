package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTest;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Long> {

    @Query("""
    SELECT dt.id
    FROM DiagnosticTest dt
    WHERE UPPER(COALESCE(dt.type, '')) = :modality
    """)
    List<Long> findDiagnosticTestIdsByModality(@Param("modality") String modality);
}