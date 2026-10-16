package com.marches.restauration.dto;

import com.marches.restauration.domain.enums.ModeReglement;

import java.time.LocalDate;

public record ReglementResponse(
        Long id,
        Long marcheId,
        String marcheReference,
        Long ordreCommandeId,
        String ordreCommandeNumero,
        String numeroOrdreCommandeSaisi,
        boolean rapprochementReussi,
        String numeroFacture,
        LocalDate dateFacture,
        Double montantHT,
        Double montantTVA,
        Double montantTTC,
        Double montantRegle,
        Double reste,
        boolean soldee,
        LocalDate dateReglement,
        ModeReglement modeReglement,
        String referencePaiement,
        String banqueCompte
) {}
