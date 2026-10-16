package com.marches.restauration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Modification d'une commande journalière existante (réservé à l'Administrateur). */
public record CommandeJournaliereModificationRequest(
        @NotNull LocalDate date,
        String lieuLivraison,
        @NotEmpty @Valid List<LigneCommandeRequest> lignes
) {}
