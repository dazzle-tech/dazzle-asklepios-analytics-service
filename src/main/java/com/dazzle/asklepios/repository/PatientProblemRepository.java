package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientProblem;
import com.dazzle.asklepios.domain.enumeration.PatientHistoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientProblemRepository extends JpaRepository<PatientProblem, Long> {

    List<PatientProblem> findByPatientIdAndStatus(Long patientId, PatientHistoryStatus status);

}
