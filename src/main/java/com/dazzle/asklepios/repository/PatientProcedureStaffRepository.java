package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientProcedureStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientProcedureStaffRepository extends JpaRepository<PatientProcedureStaff, Long> {

    List<PatientProcedureStaff> findByPatientProcedureIdOrderByIdAsc(Long patientProcedureId);
}
