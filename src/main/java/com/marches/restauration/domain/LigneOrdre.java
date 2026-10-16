package com.marches.restauration.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ligne d'un ordre de commande : pour une prestation donnée, la quantité
 * prévue sur cet ordre (c'est ce solde que les commandes journalières
 * viendront consommer progressivement).
 */
@Entity
@Table(name = "lignes_ordre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneOrdre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordre_commande_id", nullable = false)
    private OrdreCommande ordreCommande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestation_id", nullable = false)
    private Prestation prestation;

    @Column(nullable = false)
    private Integer quantitePrevue;
}
