package com.marches.restauration.dto;

public record StatistiquesMarcheDTO(
        Long marcheId,
        String reference,
        Double montantExecute,
        Double montantFacture,
        Double montantEncaisse,
        Double restantARecevoir
) {}
