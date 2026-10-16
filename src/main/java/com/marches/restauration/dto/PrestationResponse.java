package com.marches.restauration.dto;

public record PrestationResponse(
        Long id,
        String nom,
        Double prixUnitaire,
        Integer quantitePrevue,
        Double tauxTVA
) {}
