package com.marches.restauration.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Une PRESTATION est toujours rattachée à un marché : son prix unitaire, sa
 * quantité prévue et son taux de TVA sont propres à ce marché (deux marchés
 * peuvent facturer la même prestation à des prix différents — règle de gestion §5).
 */
@Entity
@Table(name = "prestations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marche_id", nullable = false)
    private Marche marche;

    /** Type prédéfini (Pause-café, Pause-déjeuner, ...) ou libellé libre via "Autre". */
    @Column(nullable = false)
    private String nom;

    /** Prix unitaire pour ce marché. */
    @Column(nullable = false)
    private Double prixUnitaire;

    /** Quantité prévue au marché pour cette prestation, saisie à la création du marché. */
    @Column(nullable = false)
    private Integer quantitePrevue;

    /** Taux de TVA propre à cette prestation (peut différer d'une prestation à l'autre). */
    @Column(nullable = false)
    private Double tauxTVA;
}
