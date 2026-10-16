package com.marches.restauration.dto;

import java.util.List;

public record CommanditaireDTO(
        String nom,
        List<ContratCommanditaireDTO> contrats,
        Double montantPrevuTotal,
        Double montantExecuteTotal
) {}
