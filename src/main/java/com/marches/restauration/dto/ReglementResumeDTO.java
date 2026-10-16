package com.marches.restauration.dto;

import java.time.LocalDate;

/** Résumé compact d'un règlement, utilisé dans le détail dépliable d'un contrat (vue Commanditaires). */
public record ReglementResumeDTO(
        Long id,
        String numeroFacture,
        LocalDate dateFacture,
        Double montantTTC,
        Double montantRegle,
        String modeReglement
) {}
