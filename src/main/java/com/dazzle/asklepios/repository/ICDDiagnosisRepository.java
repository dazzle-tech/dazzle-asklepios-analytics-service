package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ICDDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ICDDiagnosisRepository extends JpaRepository<ICDDiagnosis, Long> {

    List<ICDDiagnosis> findByIcdCodeIn(Collection<String> icdCodes);
}
