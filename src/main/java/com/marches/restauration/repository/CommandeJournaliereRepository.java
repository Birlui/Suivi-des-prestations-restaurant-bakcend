package com.marches.restauration.repository;

import com.marches.restauration.domain.CommandeJournaliere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CommandeJournaliereRepository extends JpaRepository<CommandeJournaliere, Long> {

    List<CommandeJournaliere> findByOrdreCommandeId(Long ordreCommandeId);

    List<CommandeJournaliere> findByDate(LocalDate date);

    List<CommandeJournaliere> findByLotPeriodeId(String lotPeriodeId);
}
