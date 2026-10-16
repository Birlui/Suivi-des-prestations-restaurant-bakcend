package com.marches.restauration.dto;

import java.time.LocalDate;
import java.util.List;

/** Résumé d'un ordre de commande avec ses règlements rapprochés, pour le détail dépliable d'un contrat. */
public record OrdreCommandeResumeDTO(
        Long id,
        String numero,
        LocalDate date,
        String statut,
        Double montantExecute,
        Double montantRegle,
        Double montantFacture,
        List<ReglementResumeDTO> reglements
) {}
