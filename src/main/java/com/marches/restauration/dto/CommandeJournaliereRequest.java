package com.marches.restauration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Requête de saisie d'une commande journalière.
 * <p>
 * - Jour unique : renseigner {@code date} et laisser {@code dateDebut}/{@code dateFin} vides.
 * - Activité sur plusieurs jours consécutifs (ex. séminaire d'une semaine) :
 *   renseigner {@code dateDebut} et {@code dateFin} ({@code date} est alors ignoré).
 *   Une commande journalière distincte sera créée pour chaque jour de la période,
 *   avec les mêmes lignes et le même lieu de livraison (règle de gestion §5).
 */
public record CommandeJournaliereRequest(
        @NotNull Long ordreId,
        LocalDate date,
        LocalDate dateDebut,
        LocalDate dateFin,
        String lieuLivraison,
        @NotEmpty @Valid List<LigneCommandeRequest> lignes
) {
    public boolean estSurPeriode() {
        return dateDebut != null && dateFin != null;
    }
}
