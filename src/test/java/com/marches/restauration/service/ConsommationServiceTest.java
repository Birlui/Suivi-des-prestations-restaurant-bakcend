package com.marches.restauration.service;

import com.marches.restauration.domain.*;
import com.marches.restauration.domain.enums.StatutCommandeJournaliere;
import com.marches.restauration.domain.enums.StatutOrdreCommande;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConsommationServiceTest {

    private final ConsommationService service = new ConsommationService();

    @Test
    void calculeLeSoldeEnExcluantLesCommandesAnnulees() {
        Marche marche = Marche.builder().id(1L).reference("MR-TEST").objet("Test").commanditaire("Client Test").build();
        Prestation pauseCafe = Prestation.builder().id(1L).marche(marche).nom("Pause-café").prixUnitaire(1500.0).quantitePrevue(100).tauxTVA(18.0).build();

        OrdreCommande ordre = OrdreCommande.builder().id(1L).numero("OC-2026-001").date(LocalDate.now()).marche(marche).statut(StatutOrdreCommande.ACTIF).build();
        ordre.ajouterLigne(LigneOrdre.builder().prestation(pauseCafe).quantitePrevue(50).build());

        CommandeJournaliere commandeActive = CommandeJournaliere.builder().date(LocalDate.now()).statut(StatutCommandeJournaliere.ACTIVE).build();
        commandeActive.ajouterLigne(LigneCommande.builder().prestation(pauseCafe).quantite(20).build());
        ordre.getCommandesJournalieres().add(commandeActive);
        commandeActive.setOrdreCommande(ordre);

        CommandeJournaliere commandeAnnulee = CommandeJournaliere.builder().date(LocalDate.now()).statut(StatutCommandeJournaliere.ANNULEE).build();
        commandeAnnulee.ajouterLigne(LigneCommande.builder().prestation(pauseCafe).quantite(15).build());
        ordre.getCommandesJournalieres().add(commandeAnnulee);
        commandeAnnulee.setOrdreCommande(ordre);

        Map<Long, ConsommationService.Solde> soldes = service.calculerSoldesParPrestation(ordre);

        // Seule la commande ACTIVE (20) doit compter ; la commande ANNULÉE (15) est ignorée.
        ConsommationService.Solde solde = soldes.get(pauseCafe.getId());
        assertThat(solde.prevue()).isEqualTo(50);
        assertThat(solde.consommee()).isEqualTo(20);
        assertThat(solde.restante()).isEqualTo(30);
    }

    @Test
    void calculeLeMontantExecute() {
        Marche marche = Marche.builder().id(1L).reference("MR-TEST").objet("Test").commanditaire("Client Test").build();
        Prestation pauseCafe = Prestation.builder().id(1L).marche(marche).nom("Pause-café").prixUnitaire(1500.0).quantitePrevue(100).tauxTVA(18.0).build();

        OrdreCommande ordre = OrdreCommande.builder().id(1L).numero("OC-2026-001").date(LocalDate.now()).marche(marche).statut(StatutOrdreCommande.ACTIF).build();
        ordre.ajouterLigne(LigneOrdre.builder().prestation(pauseCafe).quantitePrevue(50).build());

        CommandeJournaliere commande = CommandeJournaliere.builder().date(LocalDate.now()).statut(StatutCommandeJournaliere.ACTIVE).build();
        commande.ajouterLigne(LigneCommande.builder().prestation(pauseCafe).quantite(10).build());
        ordre.getCommandesJournalieres().add(commande);
        commande.setOrdreCommande(ordre);

        double montant = service.montantExecute(ordre);

        assertThat(montant).isEqualTo(10 * 1500.0);
    }
}
