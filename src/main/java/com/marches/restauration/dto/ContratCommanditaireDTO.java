package com.marches.restauration.dto;

import java.util.List;

/** Un contrat (marché) au sein de la fiche consolidée d'un commanditaire. */
public record ContratCommanditaireDTO(
        Long marcheId,
        String reference,
        String objet,
        String statut,
        List<PrestationConsommationDTO> prestations,
        Double montantPrevu,
        Double montantExecute,
        Double montantRegle,
        Double montantRestant,
        List<OrdreCommandeResumeDTO> ordresCommande,
        List<ReglementResumeDTO> reglementsGlobauxMarche
) {}
