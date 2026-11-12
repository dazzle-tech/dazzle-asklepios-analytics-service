package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BrandMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BrandMedicationRepository extends JpaRepository<BrandMedication, Long> {
}
