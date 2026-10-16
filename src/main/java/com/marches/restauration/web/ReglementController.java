package com.marches.restauration.web;

import com.marches.restauration.dto.ReglementRequest;
import com.marches.restauration.dto.ReglementResponse;
import com.marches.restauration.service.ReglementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Écran "Règlements / Paiements" — Administrateur et Comptable (section 6).
 * Le numéro d'ordre de commande est un champ de texte libre côté requête
 * (voir {@link ReglementRequest}) : le Comptable peut saisir n'importe quel
 * numéro, y compris un numéro non encore connu du système.
 */
@RestController
@RequestMapping("/api/reglements")
@RequiredArgsConstructor
public class ReglementController {

    private final ReglementService reglementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public List<ReglementResponse> lister(@RequestParam(required = false) Long marcheId) {
        return reglementService.lister(marcheId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public ResponseEntity<ReglementResponse> creer(@Valid @RequestBody ReglementRequest requete, Authentication auth) {
        ReglementResponse cree = reglementService.creer(requete, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }
}
