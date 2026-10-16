package com.marches.restauration.web;

import com.marches.restauration.dto.AlerteDTO;
import com.marches.restauration.dto.StatistiquesGlobalesDTO;
import com.marches.restauration.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Écran "Tableau de bord" — Administrateur et Comptable (section 6) :
 * montants exécutés / facturés / encaissés, et système d'alertes (section 7).
 */
@RestController
@RequestMapping("/api/tableau-de-bord")
@RequiredArgsConstructor
public class TableauDeBordController {

    private final StatistiquesService statistiquesService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public StatistiquesGlobalesDTO statistiques() {
        return statistiquesService.statistiquesGlobales();
    }

    @GetMapping("/alertes")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public List<AlerteDTO> alertes() {
        return statistiquesService.calculerAlertes();
    }
}
