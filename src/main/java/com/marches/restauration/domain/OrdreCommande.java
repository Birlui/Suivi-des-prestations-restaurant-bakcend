package com.marches.restauration.domain;

import com.marches.restauration.domain.enums.StatutOrdreCommande;
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
 * L'ORDRE DE COMMANDE découle d'un marché : il fixe, pour un sous-ensemble de
 * prestations, la quantité prévue qui pourra être consommée jour après jour
 * par des commandes journalières.
 * <p>
 * Le numéro est généré automatiquement par le système (format OC-AAAA-XXX) —
 * il n'est jamais saisi manuellement (règle de gestion §5).
 */
@Entity
@Table(name = "ordres_commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant métier généré automatiquement, ex. OC-2026-047. Non modifiable par l'utilisateur. */
    @Column(nullable = false, unique = true)
    private String numero;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marche_id", nullable = false)
    private Marche marche;

    @OneToMany(mappedBy = "ordreCommande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneOrdre> lignes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutOrdreCommande statut = StatutOrdreCommande.ACTIF;

    /** Pièce justificative de l'ordre (chemin ou URL de stockage). */
    private String documentJustificatif;

    @OneToMany(mappedBy = "ordreCommande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommandeJournaliere> commandesJournalieres = new ArrayList<>();

    public void ajouterLigne(LigneOrdre ligne) {
        ligne.setOrdreCommande(this);
        this.lignes.add(ligne);
    }
}
