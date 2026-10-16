package com.marches.restauration.web;

import com.marches.restauration.dto.OrdreCommandeRequest;
import com.marches.restauration.dto.OrdreCommandeResponse;
import com.marches.restauration.service.OrdreCommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Écran "Gestion des ordres de commande" — Administrateur et Gestionnaire des
 * commandes (section 6). Le numéro de l'ordre est TOUJOURS généré par le
 * serveur : il n'apparaît jamais dans {@link OrdreCommandeRequest}.
 */
@RestController
@RequestMapping("/api/ordres")
@RequiredArgsConstructor
public class OrdreCommandeController {

    private final OrdreCommandeService ordreCommandeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'GESTIONNAIRE_DES_COMMANDES')")
    public List<OrdreCommandeResponse> lister(@RequestParam(required = false) Long marcheId) {
        return ordreCommandeService.lister(marcheId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'GESTIONNAIRE_DES_COMMANDES')")
    public OrdreCommandeResponse obtenir(@PathVariable Long id) {
        return ordreCommandeService.obtenir(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'GESTIONNAIRE_DES_COMMANDES')")
    public ResponseEntity<OrdreCommandeResponse> creer(@Valid @RequestBody OrdreCommandeRequest requete, Authentication auth) {
        OrdreCommandeResponse cree = ordreCommandeService.creer(requete, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }
}
