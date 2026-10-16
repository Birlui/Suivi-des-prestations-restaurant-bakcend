package com.marches.restauration.dto;

import com.marches.restauration.domain.enums.ModeReglement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/**
 * Requête d'enregistrement d'un règlement par le Comptable.
 * <p>
 * {@code numeroOrdreCommande} est un champ de TEXTE LIBRE : le Comptable peut
 * taper n'importe quel numéro (avec suggestion des ordres existants côté
 * front-end), y compris un numéro qui ne correspond à aucun ordre encore
 * connu du système. Laissé vide, le règlement est traité comme une facture
 * globale du marché (voir ReglementService#rapprocherOrdre).
 */
public record ReglementRequest(
        @NotNull Long marcheId,
        String numeroOrdreCommande,
        @NotBlank String numeroFacture,
        @NotNull LocalDate dateFacture,
        @NotNull @PositiveOrZero Double montantHT,
        @NotNull @PositiveOrZero Double montantTVA,
        @NotNull @PositiveOrZero Double montantTTC,
        @PositiveOrZero Double montantRegle,
        LocalDate dateReglement,
        ModeReglement modeReglement,
        String referencePaiement,
        String banqueCompte,
        String pieceJustificative
) {}
