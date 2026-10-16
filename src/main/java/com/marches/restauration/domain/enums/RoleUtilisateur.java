package com.marches.restauration.domain.enums;

/**
 * Les 4 profils utilisateurs de l'application, avec des droits d'accès
 * strictement différenciés (voir section 4 du cahier des charges).
 */
public enum RoleUtilisateur {
    ADMINISTRATEUR,
    COMPTABLE,
    GESTIONNAIRE_DES_COMMANDES,
    CUISINIER
}
