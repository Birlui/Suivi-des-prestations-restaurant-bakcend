package com.marches.restauration.repository;

import com.marches.restauration.domain.Marche;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarcheRepository extends JpaRepository<Marche, Long> {
    Optional<Marche> findByReference(String reference);
    boolean existsByReference(String reference);
}
