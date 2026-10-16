package com.marches.restauration.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LigneCommandeRequest(
        @NotNull Long prestationId,
        @NotNull @Positive Integer quantite
) {}
