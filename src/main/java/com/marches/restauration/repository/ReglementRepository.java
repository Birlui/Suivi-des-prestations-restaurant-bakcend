package com.marches.restauration.repository;

import com.marches.restauration.domain.Reglement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReglementRepository extends JpaRepository<Reglement, Long> {
    List<Reglement> findByMarcheId(Long marcheId);
    List<Reglement> findByOrdreCommandeId(Long ordreCommandeId);
}
