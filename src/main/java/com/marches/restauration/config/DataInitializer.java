package com.marches.restauration.config;

import com.marches.restauration.domain.*;
import com.marches.restauration.domain.enums.*;
import com.marches.restauration.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Recrée, au démarrage, exactement les mêmes données de démonstration que le
 * prototype front-end (mêmes marchés, prestations, ordres, commandes et
 * règlements), ainsi qu'un compte par rôle pour tester l'API immédiatement.
 * <p>
 * ⚠️ Les mots de passe ci-dessous sont volontairement simples : ils sont
 * réservés à la démonstration locale. Ne jamais les utiliser en production
 * (voir README, section "Sécurité").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MarcheRepository marcheRepository;
    private final OrdreCommandeRepository ordreCommandeRepository;
    private final CommandeJournaliereRepository commandeJournaliereRepository;
    private final ReglementRepository reglementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    /** APP_RESET_DATA=true : vide toutes les données métier au démarrage (les utilisateurs sont conservés). */
    @Value("${app.reset-data:false}")
    private boolean resetData;

    /** APP_DEMO_DATA=false : ne jamais insérer les données de démonstration. */
    @Value("${app.demo-data:true}")
    private boolean demoData;

    @Override
    @Transactional
    public void run(String... args) {
        if (resetData) {
            viderDonneesMetier();
        }
        if (!demoData) {
            log.info("Données de démonstration désactivées (app.demo-data=false).");
            return;
        }
        if (marcheRepository.count() > 0) {
            log.info("Données déjà présentes — initialisation ignorée.");
            return;
        }

        creerUtilisateursDeDemonstration();

        // ---------------------------------------------------------------
        // Marché 1 — Ministère de l'Équipement
        // ---------------------------------------------------------------
        Marche marche1 = Marche.builder()
                .reference("MR-2026-014")
                .objet("Restauration collective - Site Administratif Central")
                .commanditaire("Ministère de l'Équipement")
                .dateDebut(LocalDate.of(2026, 1, 15))
                .dateFin(LocalDate.of(2026, 12, 31))
                .tauxTVA(18.0)
                .statut(StatutMarche.EN_COURS)
                .build();
        Prestation p1 = Prestation.builder().nom("Pause-café").prixUnitaire(1500.0).quantitePrevue(500).tauxTVA(18.0).build();
        Prestation p2 = Prestation.builder().nom("Pause-déjeuner").prixUnitaire(4500.0).quantitePrevue(500).tauxTVA(18.0).build();
        Prestation p3 = Prestation.builder().nom("Cocktail").prixUnitaire(6000.0).quantitePrevue(50).tauxTVA(18.0).build();
        Prestation p4 = Prestation.builder().nom("Pause-café renforcée").prixUnitaire(2500.0).quantitePrevue(100).tauxTVA(18.0).build();
        Prestation p5 = Prestation.builder().nom("Location de salle").prixUnitaire(75000.0).quantitePrevue(12).tauxTVA(10.0).build();
        marche1.ajouterPrestation(p1);
        marche1.ajouterPrestation(p2);
        marche1.ajouterPrestation(p3);
        marche1.ajouterPrestation(p4);
        marche1.ajouterPrestation(p5);
        marche1 = marcheRepository.save(marche1);

        // ---------------------------------------------------------------
        // Marché 2 — Banque Régionale de Développement
        // ---------------------------------------------------------------
        Marche marche2 = Marche.builder()
                .reference("MR-2026-021")
                .objet("Restauration séminaires - Direction Générale")
                .commanditaire("Banque Régionale de Développement")
                .dateDebut(LocalDate.of(2026, 3, 1))
                .dateFin(LocalDate.of(2026, 9, 30))
                .tauxTVA(18.0)
                .statut(StatutMarche.EN_COURS)
                .build();
        Prestation p6 = Prestation.builder().nom("Pause-café").prixUnitaire(1800.0).quantitePrevue(250).tauxTVA(18.0).build();
        Prestation p7 = Prestation.builder().nom("Pause-déjeuner").prixUnitaire(5200.0).quantitePrevue(150).tauxTVA(18.0).build();
        marche2.ajouterPrestation(p6);
        marche2.ajouterPrestation(p7);
        marche2 = marcheRepository.save(marche2);

        // ---------------------------------------------------------------
        // Ordres de commande
        // ---------------------------------------------------------------
        OrdreCommande ordre1 = OrdreCommande.builder()
                .numero("OC-2026-045").date(LocalDate.of(2026, 2, 1)).marche(marche1).statut(StatutOrdreCommande.ACTIF)
                .build();
        ordre1.ajouterLigne(LigneOrdre.builder().prestation(p1).quantitePrevue(300).build());
        ordre1.ajouterLigne(LigneOrdre.builder().prestation(p2).quantitePrevue(300).build());
        ordre1 = ordreCommandeRepository.save(ordre1);

        OrdreCommande ordre2 = OrdreCommande.builder()
                .numero("OC-2026-046").date(LocalDate.of(2026, 3, 5)).marche(marche2).statut(StatutOrdreCommande.ACTIF)
                .build();
        ordre2.ajouterLigne(LigneOrdre.builder().prestation(p7).quantitePrevue(150).build());
        ordre2 = ordreCommandeRepository.save(ordre2);

        // ---------------------------------------------------------------
        // Commandes journalières
        // ---------------------------------------------------------------
        CommandeJournaliere cj1 = CommandeJournaliere.builder()
                .date(LocalDate.of(2026, 8, 10)).ordreCommande(ordre1)
                .lieuLivraison("Site Administratif Central - Salle A").statut(StatutCommandeJournaliere.ACTIVE)
                .saisiPar("gestionnaire").build();
        cj1.ajouterLigne(LigneCommande.builder().prestation(p1).quantite(40).build());
        cj1.ajouterLigne(LigneCommande.builder().prestation(p2).quantite(38).build());
        commandeJournaliereRepository.save(cj1);

        CommandeJournaliere cj2 = CommandeJournaliere.builder()
                .date(LocalDate.of(2026, 8, 11)).ordreCommande(ordre1)
                .lieuLivraison("Site Administratif Central - Salle A").statut(StatutCommandeJournaliere.ACTIVE)
                .saisiPar("gestionnaire").build();
        cj2.ajouterLigne(LigneCommande.builder().prestation(p1).quantite(42).build());
        cj2.ajouterLigne(LigneCommande.builder().prestation(p2).quantite(40).build());
        commandeJournaliereRepository.save(cj2);

        CommandeJournaliere cj3 = CommandeJournaliere.builder()
                .date(LocalDate.of(2026, 8, 12)).ordreCommande(ordre2)
                .lieuLivraison("Direction Générale - Salle de conférence").statut(StatutCommandeJournaliere.ACTIVE)
                .saisiPar("gestionnaire").build();
        cj3.ajouterLigne(LigneCommande.builder().prestation(p7).quantite(30).build());
        commandeJournaliereRepository.save(cj3);

        // ---------------------------------------------------------------
        // Règlements
        // ---------------------------------------------------------------
        Reglement r1 = Reglement.builder()
                .marche(marche1).ordreCommande(ordre1).numeroOrdreCommandeSaisi("OC-2026-045")
                .numeroFacture("FA-2026-101").dateFacture(LocalDate.of(2026, 8, 5))
                .montantHT(296610.17).montantTVA(53389.83).montantTTC(350000.0)
                .montantRegle(250000.0).dateReglement(LocalDate.of(2026, 8, 5)).modeReglement(ModeReglement.VIREMENT)
                .build();
        reglementRepository.save(r1);

        Reglement r2 = Reglement.builder()
                .marche(marche2).numeroOrdreCommandeSaisi("")
                .numeroFacture("FA-2026-102").dateFacture(LocalDate.of(2026, 8, 9))
                .montantHT(152542.37).montantTVA(27457.63).montantTTC(180000.0)
                .montantRegle(180000.0).dateReglement(LocalDate.of(2026, 8, 9)).modeReglement(ModeReglement.MOBILE_MONEY)
                .build();
        reglementRepository.save(r2);

        log.info("Données de démonstration initialisées : 2 marchés, 2 ordres, 3 commandes journalières, 2 règlements.");
    }

    /**
     * Supprime toutes les données métier, dans l'ordre des clés étrangères.
     * La table des utilisateurs n'est pas touchée.
     */
    private void viderDonneesMetier() {
        String[] entites = {
                "LigneCommande", "CommandeJournaliere", "LigneOrdre", "Reglement",
                "OrdreCommande", "Prestation", "Marche", "JournalOperation"
        };
        for (String entite : entites) {
            int supprimes = entityManager.createQuery("delete from " + entite).executeUpdate();
            log.warn("Réinitialisation : {} ligne(s) supprimée(s) dans {}.", supprimes, entite);
        }
        log.warn("Données métier vidées (utilisateurs conservés). Retirer APP_RESET_DATA de l'environnement.");
    }

    private void creerUtilisateursDeDemonstration() {
        Map<String, Object[]> comptes = new HashMap<>();
        // identifiant -> { nom complet, mot de passe en clair, rôle }
        comptes.put("admin", new Object[]{"Administrateur Démo", "admin123", RoleUtilisateur.ADMINISTRATEUR});
        comptes.put("comptable", new Object[]{"Comptable Démo", "comptable123", RoleUtilisateur.COMPTABLE});
        comptes.put("gestionnaire", new Object[]{"Gestionnaire Démo", "gestionnaire123", RoleUtilisateur.GESTIONNAIRE_DES_COMMANDES});
        comptes.put("cuisinier", new Object[]{"Cuisinier Démo", "cuisinier123", RoleUtilisateur.CUISINIER});

        comptes.forEach((identifiant, infos) -> {
            if (utilisateurRepository.findByIdentifiant(identifiant).isPresent()) return;
            utilisateurRepository.save(Utilisateur.builder()
                    .identifiant(identifiant)
                    .nomComplet((String) infos[0])
                    .motDePasse(passwordEncoder.encode((String) infos[1]))
                    .role((RoleUtilisateur) infos[2])
                    .actif(true)
                    .build());
        });

        log.info("Comptes de démonstration créés : admin/admin123, comptable/comptable123, "
                + "gestionnaire/gestionnaire123, cuisinier/cuisinier123 (à changer en production).");
    }
}
