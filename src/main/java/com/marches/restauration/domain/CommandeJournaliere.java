package com.marches.restauration.domain;

import com.marches.restauration.domain.enums.StatutCommandeJournaliere;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * La COMMANDE JOURNALIÈRE consomme, jour après jour, le solde d'un ordre de
 * commande. Pour une activité se déroulant sur plusieurs jours consécutifs,
 * plusieurs commandes journalières (une par jour) partagent la même origine
 * (voir CommandeJournaliereService#creerSurPeriode).
 */
@Entity
@Table(name = "commandes_journalieres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeJournaliere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordre_commande_id", nullable = false)
    private OrdreCommande ordreCommande;

    /** Lieu de livraison (site, bâtiment, salle) — visible par le Cuisinier. */
    private String lieuLivraison;

    @OneToMany(mappedBy = "commandeJournaliere", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneCommande> lignes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutCommandeJournaliere statut = StatutCommandeJournaliere.ACTIVE;

    /** Utilisateur ayant saisi la commande (Gestionnaire des commandes). */
    private String saisiPar;

    /**
     * Identifiant technique commun à toutes les commandes générées à partir
     * d'une même saisie sur une plage de dates (permet de les retrouver et,
     * si besoin, de les annuler groupées). Null pour une commande sur un jour unique.
     */
    private String lotPeriodeId;

    public void ajouterLigne(LigneCommande ligne) {
        ligne.setCommandeJournaliere(this);
        this.lignes.add(ligne);
    }
}
