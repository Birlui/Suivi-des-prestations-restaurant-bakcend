package com.marches.restauration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Requête de création d'un marché, avec ses prestations. */
public record MarcheRequest(
        @NotBlank String reference,
        @NotBlank String objet,
        @NotBlank String commanditaire,
        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin,
        Double tauxTVA,
        String documentMarche,
        String infosFiscales,
        @NotEmpty @Valid List<PrestationRequest> prestations
) {}
