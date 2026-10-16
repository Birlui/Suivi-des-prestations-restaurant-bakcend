package com.marches.restauration.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LigneOrdreRequest(
        @NotNull Long prestationId,
        @NotNull @Positive Integer quantitePrevue
) {}
