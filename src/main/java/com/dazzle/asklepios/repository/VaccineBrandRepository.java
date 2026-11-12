package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VaccineBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VaccineBrandRepository extends JpaRepository<VaccineBrand, Long> {
}
