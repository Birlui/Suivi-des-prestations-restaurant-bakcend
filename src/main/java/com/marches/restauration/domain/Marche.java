package com.marches.restauration.domain;

import com.marches.restauration.domain.enums.StatutMarche;
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
 * Le MARCHÉ est le niveau le plus élevé de la hiérarchie : c'est le contrat
 * global passé avec un commanditaire (voir section 3 du cahier des charges).
 */
@Entity
@Table(name = "marches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Référence unique du marché (ex. MR-2026-014). */
    @Column(nullable = false, unique = true)
    private String reference;

    /** Objet / intitulé du marché. */
    @Column(nullable = false)
    private String objet;

    /** Nom de l'entité commanditaire. */
    @Column(nullable = false)
    private String commanditaire;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    /** Taux de TVA par défaut du marché (chaque prestation peut le redéfinir). */
    private Double tauxTVA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutMarche statut = StatutMarche.EN_PREPARATION;

    /** Référence vers le contrat scanné / pièce jointe (chemin ou URL de stockage). */
    private String documentMarche;

    /** Références d'enregistrement fiscal. */
    private String infosFiscales;

    @OneToMany(mappedBy = "marche", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Prestation> prestations = new ArrayList<>();

    @OneToMany(mappedBy = "marche", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrdreCommande> ordresCommande = new ArrayList<>();

    public void ajouterPrestation(Prestation prestation) {
        prestation.setMarche(this);
        this.prestations.add(prestation);
    }
}
