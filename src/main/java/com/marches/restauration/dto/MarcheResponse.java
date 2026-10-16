package com.marches.restauration.dto;

import com.marches.restauration.domain.enums.StatutMarche;

import java.time.LocalDate;
import java.util.List;

public record MarcheResponse(
        Long id,
        String reference,
        String objet,
        String commanditaire,
        LocalDate dateDebut,
        LocalDate dateFin,
        Double tauxTVA,
        StatutMarche statut,
        String documentMarche,
        String infosFiscales,
        List<PrestationResponse> prestations
) {}
