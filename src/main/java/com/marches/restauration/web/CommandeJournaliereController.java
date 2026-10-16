package com.marches.restauration.web;

import com.marches.restauration.dto.CommandeJournaliereModificationRequest;
import com.marches.restauration.dto.CommandeJournaliereRequest;
import com.marches.restauration.dto.CommandeJournaliereResponse;
import com.marches.restauration.service.CommandeJournaliereService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Écran "Saisie des commandes journalières" (Administrateur, Gestionnaire des
 * commandes) et "Consultation cuisine" en lecture seule (Cuisinier) — section 6.
 * <p>
 * Le Cuisinier n'a pas accès aux données financières (section 4) : les
 * montants sont masqués dans les réponses qui lui sont servies, et sa
 * consultation est, par défaut, limitée aux commandes du jour.
 */
@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeJournaliereController {

    private final CommandeJournaliereService commandeJournaliereService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'GESTIONNAIRE_DES_COMMANDES', 'CUISINIER')")
    public List<CommandeJournaliereResponse> lister(
            @RequestParam(required = false) Long ordreId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {

        boolean estCuisinier = estCuisinier(auth);
        // Le Cuisinier ne voit que les commandes du jour, sauf s'il précise explicitement une autre date.
        LocalDate dateEffective = (date == null && estCuisinier) ? LocalDate.now() : date;
        return commandeJournaliereService.lister(ordreId, dateEffective, estCuisinier);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'GESTIONNAIRE_DES_COMMANDES', 'CUISINIER')")
    public CommandeJournaliereResponse obtenir(@PathVariable Long id, Authentication auth) {
        return commandeJournaliereService.obtenir(id, estCuisinier(auth));
    }

    /**
     * Création d'une ou plusieurs commandes : une seule si {@code date} est renseignée,
     * une par jour de la période si {@code dateDebut}/{@code dateFin} sont renseignées
     * (activité pluri-journalière — règle de gestion §5).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'GESTIONNAIRE_DES_COMMANDES')")
    public ResponseEntity<List<CommandeJournaliereResponse>> creer(
            @Valid @RequestBody CommandeJournaliereRequest requete, Authentication auth) {
        List<CommandeJournaliereResponse> creees = commandeJournaliereService.creer(requete, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creees);
    }

    /** Modification réservée à l'Administrateur. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public CommandeJournaliereResponse modifier(
            @PathVariable Long id, @Valid @RequestBody CommandeJournaliereModificationRequest requete, Authentication auth) {
        return commandeJournaliereService.modifier(id, requete, auth.getName());
    }

    /**
     * Annulation réservée à l'Administrateur et au Comptable : le Gestionnaire
     * qui a saisi la commande ne peut pas l'annuler lui-même (règle de gestion §5).
     */
    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'COMPTABLE')")
    public CommandeJournaliereResponse annuler(@PathVariable Long id, Authentication auth) {
        return commandeJournaliereService.annuler(id, auth.getName());
    }

    private boolean estCuisinier(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CUISINIER"));
    }
}
