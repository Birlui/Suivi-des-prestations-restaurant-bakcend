package com.marches.restauration.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Journal d'audit horodaté : toute création ou modification de donnée
 * (marché, ordre, commande, règlement) doit être journalisée avec l'auteur,
 * la date et l'heure — règle de gestion §5 / architecture technique §9.
 */
@Entity
@Table(name = "journal_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private LocalDateTime dateHeure = LocalDateTime.now();

    /** Identifiant de l'utilisateur authentifié à l'origine de l'opération. */
    private String auteur;

    /** Ex. CREATION, MODIFICATION, ANNULATION. */
    @Column(nullable = false)
    private String action;

    /** Ex. MARCHE, ORDRE_COMMANDE, COMMANDE_JOURNALIERE, REGLEMENT. */
    @Column(nullable = false)
    private String entite;

    private String entiteId;

    @Column(length = 2000)
    private String details;
}
