package com.marches.restauration.dto;

/** Ligne d'ordre enrichie du solde (prévue / consommée / restante), comme dans l'écran "Ordres de commande". */
public record LigneOrdreResponse(
        Long prestationId,
        String prestationNom,
        Double prixUnitaire,
        Integer quantitePrevue,
        Integer quantiteConsommee,
        Integer quantiteRestante
) {}
