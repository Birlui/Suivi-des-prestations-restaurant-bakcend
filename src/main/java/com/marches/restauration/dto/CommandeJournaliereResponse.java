package com.marches.restauration.dto;

import com.marches.restauration.domain.enums.StatutCommandeJournaliere;

import java.time.LocalDate;
import java.util.List;

public record CommandeJournaliereResponse(
        Long id,
        LocalDate date,
        Long ordreId,
        String ordreNumero,
        Long marcheId,
        String marcheReference,
        String lieuLivraison,
        StatutCommandeJournaliere statut,
        List<LigneCommandeResponse> lignes,
        Double montant,
        String lotPeriodeId
) {}
