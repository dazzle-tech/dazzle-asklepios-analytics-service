package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ProgressNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressNoteRepository
        extends JpaRepository<ProgressNote, Long> {

    Optional<ProgressNote> findFirstByEncounterIdAndCancelledByIsNullOrderByCreatedDateDesc(Long encounterId);

}
