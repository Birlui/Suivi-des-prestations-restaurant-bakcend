package com.marches.restauration.dto;

public record LigneCommandeResponse(
        Long prestationId,
        String prestationNom,
        Integer quantite,
        Double montant
) {}
