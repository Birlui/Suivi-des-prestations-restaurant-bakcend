package com.marches.restauration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Prestation saisie à la création (ou modification) d'un marché. */
public record PrestationRequest(
        @NotBlank String nom,
        @NotNull @PositiveOrZero Double prixUnitaire,
        @NotNull @PositiveOrZero Integer quantitePrevue,
        @NotNull @PositiveOrZero Double tauxTVA
) {}
