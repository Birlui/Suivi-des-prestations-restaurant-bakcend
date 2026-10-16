package com.marches.restauration.web;

import com.marches.restauration.dto.CommanditaireDTO;
import com.marches.restauration.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Écran "Commanditaires" — Administrateur et Comptable (section 6) : vue
 * consolidée par commanditaire et par contrat, avec colonnes dynamiques par
 * prestation (total / servi / restant) et montants (prévu / exécuté / réglé / restant).
 */
@RestController
@RequestMapping("/api/commanditaires")
@RequiredArgsConstructor
public class CommanditaireController {

    private final StatistiquesService statistiquesService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public List<CommanditaireDTO> lister() {
        return statistiquesService.commanditaires();
    }
}
