package com.marches.restauration.config;

import com.marches.restauration.domain.Utilisateur;
import com.marches.restauration.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Charge les utilisateurs depuis la table UTILISATEUR pour l'authentification
 * HTTP Basic. Le rôle métier (ex. ADMINISTRATEUR) devient l'autorité Spring
 * Security "ROLE_ADMINISTRATEUR", utilisée par les annotations @PreAuthorize.
 */
@Service
@RequiredArgsConstructor
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(identifiant)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + identifiant));

        return User.builder()
                .username(utilisateur.getIdentifiant())
                .password(utilisateur.getMotDePasse())
                .disabled(!utilisateur.isActif())
                .authorities("ROLE_" + utilisateur.getRole().name())
                .build();
    }
}
