package com.marches.restauration.repository;

import com.marches.restauration.domain.JournalOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalOperationRepository extends JpaRepository<JournalOperation, Long> {
}
