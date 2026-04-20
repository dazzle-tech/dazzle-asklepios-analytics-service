package com.dazzle.asklepios.repository;


import com.dazzle.asklepios.domain.PrescriptionInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionInstructionRepository extends JpaRepository<PrescriptionInstruction, Long> {


}
