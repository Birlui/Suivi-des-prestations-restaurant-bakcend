package com.marches.restauration.dto;

/**
 * Détail d'une prestation pour l'écran "Commanditaires" : quantité totale
 * inscrite au contrat, quantité déjà servie, et quantité restante.
 */
public record PrestationConsommationDTO(
        String nom,
        Integer total,
        Integer servi,
        Integer restant
) {}
