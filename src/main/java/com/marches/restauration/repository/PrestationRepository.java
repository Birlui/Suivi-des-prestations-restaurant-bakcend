package com.marches.restauration.repository;

import com.marches.restauration.domain.Prestation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestationRepository extends JpaRepository<Prestation, Long> {
    List<Prestation> findByMarcheId(Long marcheId);
}
