package com.marches.restauration.dto;

import com.marches.restauration.domain.enums.StatutOrdreCommande;

import java.time.LocalDate;
import java.util.List;

public record OrdreCommandeResponse(
        Long id,
        String numero,
        LocalDate date,
        Long marcheId,
        String marcheReference,
        StatutOrdreCommande statut,
        List<LigneOrdreResponse> lignes,
        Double montantHT,
        Double montantTVA,
        Double montantTTC
) {}
