package com.marches.restauration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Requête de création d'un ordre de commande. Le numéro n'est volontairement
 * pas un champ de cette requête : il est toujours généré par le serveur
 * (voir OrdreCommandeService#genererProchainNumero).
 */
public record OrdreCommandeRequest(
        @NotNull Long marcheId,
        @NotNull LocalDate date,
        @NotEmpty @Valid List<LigneOrdreRequest> lignes
) {}
