package com.marches.restauration.domain;

import com.marches.restauration.domain.enums.ModeReglement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * FACTURE / RÈGLEMENT : rattaché obligatoirement à un marché, et de façon
 * optionnelle à un ordre de commande précis. Si aucun ordre n'est identifié,
 * le règlement est traité comme une facture globale du marché (couvrant
 * l'ensemble de ses ordres) — voir règle de gestion §5 et section 6.
 * <p>
 * Le numéro d'ordre de commande peut être saisi librement par le Comptable
 * (texte) ; le système tente de le rapprocher d'un ordre existant du même
 * marché ({@link #ordreCommande}), mais conserve toujours le texte saisi
 * ({@link #numeroOrdreCommandeSaisi}) même si aucun rapprochement n'a été possible.
 */
@Entity
@Table(name = "reglements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reglement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marche_id", nullable = false)
    private Marche marche;

    /** Ordre de commande rapproché automatiquement à partir du texte saisi (peut être null). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_commande_id")
    private OrdreCommande ordreCommande;

    /** Numéro d'ordre de commande tel que saisi librement par le Comptable (peut ne correspondre à aucun ordre connu). */
    private String numeroOrdreCommandeSaisi;

    @Column(nullable = false)
    private String numeroFacture;

    @Column(nullable = false)
    private LocalDate dateFacture;

    @Column(nullable = false)
    private Double montantHT;

    @Column(nullable = false)
    private Double montantTVA;

    @Column(nullable = false)
    private Double montantTTC;

    @Builder.Default
    private Double montantRegle = 0.0;

    private LocalDate dateReglement;

    @Enumerated(EnumType.STRING)
    private ModeReglement modeReglement;

    /** N° de transaction, de chèque, etc. */
    private String referencePaiement;

    /** Banque ou compte destinataire. */
    private String banqueCompte;

    /** Justificatif de paiement (chemin ou URL de stockage). */
    private String pieceJustificative;

    @Transient
    public Double getReste() {
        return montantTTC - (montantRegle == null ? 0.0 : montantRegle);
    }

    @Transient
    public boolean isSoldee() {
        return getReste() <= 0.0;
    }
}
