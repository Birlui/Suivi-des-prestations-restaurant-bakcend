package com.marches.restauration.service;

import com.marches.restauration.domain.CommandeJournaliere;
import com.marches.restauration.domain.LigneCommande;
import com.marches.restauration.domain.LigneOrdre;
import com.marches.restauration.domain.OrdreCommande;
import com.marches.restauration.domain.enums.StatutCommandeJournaliere;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calcule, pour un ordre de commande, le solde (prévue / consommée / restante)
 * de chacune de ses prestations — logique centrale reprise du "ordreConsommation"
 * du prototype front-end. Une commande journalière au statut ANNULÉE ne compte
 * jamais dans la consommation (règle de gestion §5).
 */
@Service
public class ConsommationService {

    public record Solde(int prevue, int consommee) {
        public int restante() {
            return prevue - consommee;
        }
    }

    /** Soldes par identifiant de prestation, pour un ordre donné. */
    public Map<Long, Solde> calculerSoldesParPrestation(OrdreCommande ordre) {
        Map<Long, Integer> prevues = new LinkedHashMap<>();
        for (LigneOrdre ligne : ordre.getLignes()) {
            prevues.put(ligne.getPrestation().getId(), ligne.getQuantitePrevue());
        }

        Map<Long, Integer> consommees = new HashMap<>();
        for (CommandeJournaliere commande : ordre.getCommandesJournalieres()) {
            if (commande.getStatut() == StatutCommandeJournaliere.ANNULEE) continue;
            for (LigneCommande ligne : commande.getLignes()) {
                consommees.merge(ligne.getPrestation().getId(), ligne.getQuantite(), Integer::sum);
            }
        }

        Map<Long, Solde> resultat = new LinkedHashMap<>();
        prevues.forEach((prestationId, prevue) ->
                resultat.put(prestationId, new Solde(prevue, consommees.getOrDefault(prestationId, 0))));
        return resultat;
    }

    /**
     * Quantité que la commande {@code commande} contribue elle-même à la consommation
     * d'une prestation donnée. Utilisé pour recalculer le solde disponible "hors
     * cette commande" lors d'une modification (on ne doit pas se pénaliser soi-même).
     */
    public int consommeeParCommande(CommandeJournaliere commande, Long prestationId) {
        if (commande.getStatut() == StatutCommandeJournaliere.ANNULEE) return 0;
        return commande.getLignes().stream()
                .filter(l -> l.getPrestation().getId().equals(prestationId))
                .mapToInt(LigneCommande::getQuantite)
                .sum();
    }

    /** Montant exécuté (quantité consommée × prix unitaire) sur l'ensemble d'un ordre. */
    public double montantExecute(OrdreCommande ordre) {
        Map<Long, Solde> soldes = calculerSoldesParPrestation(ordre);
        double total = 0;
        for (LigneOrdre ligne : ordre.getLignes()) {
            Solde solde = soldes.get(ligne.getPrestation().getId());
            if (solde != null) {
                total += solde.consommee() * ligne.getPrestation().getPrixUnitaire();
            }
        }
        return total;
    }
}
