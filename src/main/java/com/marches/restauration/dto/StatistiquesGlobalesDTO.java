package com.marches.restauration.dto;

import java.util.List;

public record StatistiquesGlobalesDTO(
        Double montantExecuteTotal,
        Double montantFactureTotal,
        Double montantEncaisseTotal,
        Double restantARecevoirTotal,
        Long commandesAujourdhui,
        List<AlerteDTO> alertes
) {}
