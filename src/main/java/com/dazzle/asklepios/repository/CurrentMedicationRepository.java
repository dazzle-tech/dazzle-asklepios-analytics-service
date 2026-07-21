package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CurrentMedication;
import com.dazzle.asklepios.domain.enumeration.PatientHistoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrentMedicationRepository extends JpaRepository<CurrentMedication, Long> {

    List<CurrentMedication> findByPatientIdAndStatus(Long patientId, PatientHistoryStatus status);

}
