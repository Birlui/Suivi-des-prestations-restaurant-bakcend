package com.marches.restauration.dto;

import com.marches.restauration.domain.enums.RoleUtilisateur;

public record UtilisateurResponse(
        String identifiant,
        String nomComplet,
        RoleUtilisateur role
) {}
