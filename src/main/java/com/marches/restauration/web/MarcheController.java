package com.marches.restauration.web;

import com.marches.restauration.dto.MarcheRequest;
import com.marches.restauration.dto.MarcheResponse;
import com.marches.restauration.service.MarcheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Écran "Gestion des marchés" — Administrateur et Comptable (section 6).
 */
@RestController
@RequestMapping("/api/marches")
@RequiredArgsConstructor
public class MarcheController {

    private final MarcheService marcheService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public List<MarcheResponse> lister() {
        return marcheService.lister();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public MarcheResponse obtenir(@PathVariable Long id) {
        return marcheService.obtenir(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public ResponseEntity<MarcheResponse> creer(@Valid @RequestBody MarcheRequest requete, Authentication auth) {
        MarcheResponse cree = marcheService.creer(requete, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    /** Un marché ne peut jamais être supprimé, uniquement clôturé (règle de gestion §5). */
    @PatchMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public MarcheResponse cloturer(@PathVariable Long id, Authentication auth) {
        return marcheService.cloturer(id, auth.getName());
    }
}
