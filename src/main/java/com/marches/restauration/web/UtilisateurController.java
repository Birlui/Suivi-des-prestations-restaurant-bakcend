package com.marches.restauration.web;

import com.marches.restauration.domain.Utilisateur;
import com.marches.restauration.dto.UtilisateurResponse;
import com.marches.restauration.exception.RessourceNonTrouveeException;
import com.marches.restauration.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Permet à un client (le front-end) de savoir qui est authentifié et quel est
 * son rôle, juste après la saisie des identifiants (HTTP Basic). C'est ce rôle
 * qui pilote l'affichage du menu et des actions autorisées côté interface.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/moi")
    public UtilisateurResponse moi(Authentication auth) {
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(auth.getName())
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable : " + auth.getName()));
        return new UtilisateurResponse(utilisateur.getIdentifiant(), utilisateur.getNomComplet(), utilisateur.getRole());
    }
}
