package com.marches.restauration.service;

import com.marches.restauration.domain.LigneOrdre;
import com.marches.restauration.domain.Marche;
import com.marches.restauration.domain.OrdreCommande;
import com.marches.restauration.domain.Prestation;
import com.marches.restauration.domain.Reglement;
import com.marches.restauration.dto.AlerteDTO;
import com.marches.restauration.dto.CommanditaireDTO;
import com.marches.restauration.dto.ContratCommanditaireDTO;
import com.marches.restauration.dto.OrdreCommandeResumeDTO;
import com.marches.restauration.dto.PrestationConsommationDTO;
import com.marches.restauration.dto.ReglementResumeDTO;
import com.marches.restauration.dto.StatistiquesGlobalesDTO;
import com.marches.restauration.dto.StatistiquesMarcheDTO;
import com.marches.restauration.repository.CommandeJournaliereRepository;
import com.marches.restauration.repository.MarcheRepository;
import com.marches.restauration.repository.OrdreCommandeRepository;
import com.marches.restauration.repository.ReglementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Regroupe les calculs transverses : statistiques par marché et globales,
 * système d'alertes (section 7 du cahier des charges) et vue consolidée par
 * commanditaire (section 6 — écran "Commanditaires").
 */
@Service
@RequiredArgsConstructor
public class StatistiquesService {

    private static final double SEUIL_ALERTE_QUANTITE_FAIBLE = 0.8;

    private final MarcheRepository marcheRepository;
    private final OrdreCommandeRepository ordreCommandeRepository;
    private final ReglementRepository reglementRepository;
    private final CommandeJournaliereRepository commandeJournaliereRepository;
    private final ConsommationService consommationService;

    // ------------------------------------------------------------------
    // Statistiques par marché / globales
    // ------------------------------------------------------------------

    public StatistiquesMarcheDTO statistiquesMarche(Marche marche) {
        List<OrdreCommande> ordres = ordreCommandeRepository.findByMarcheId(marche.getId());
        double montantExecute = ordres.stream().mapToDouble(consommationService::montantExecute).sum();

        List<Reglement> reglements = reglementRepository.findByMarcheId(marche.getId());
        double facture = reglements.stream().mapToDouble(Reglement::getMontantTTC).sum();
        double encaisse = reglements.stream().mapToDouble(r -> r.getMontantRegle() == null ? 0 : r.getMontantRegle()).sum();

        return new StatistiquesMarcheDTO(marche.getId(), marche.getReference(), montantExecute, facture, encaisse, facture - encaisse);
    }

    @Transactional(readOnly = true)
    public StatistiquesGlobalesDTO statistiquesGlobales() {
        List<Marche> marches = marcheRepository.findAll();
        double executeTotal = 0, factureTotal = 0, encaisseTotal = 0;
        for (Marche marche : marches) {
            StatistiquesMarcheDTO s = statistiquesMarche(marche);
            executeTotal += s.montantExecute();
            factureTotal += s.montantFacture();
            encaisseTotal += s.montantEncaisse();
        }
        long commandesAujourdhui = commandeJournaliereRepository.findByDate(LocalDate.now()).size();
        return new StatistiquesGlobalesDTO(
                executeTotal, factureTotal, encaisseTotal, factureTotal - encaisseTotal,
                commandesAujourdhui, calculerAlertes()
        );
    }

    // ------------------------------------------------------------------
    // Système d'alertes (section 7)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AlerteDTO> calculerAlertes() {
        List<AlerteDTO> alertes = new ArrayList<>();

        for (OrdreCommande ordre : ordreCommandeRepository.findAll()) {
            Map<Long, ConsommationService.Solde> soldes = consommationService.calculerSoldesParPrestation(ordre);
            for (LigneOrdre ligne : ordre.getLignes()) {
                ConsommationService.Solde solde = soldes.get(ligne.getPrestation().getId());
                if (solde == null) continue;
                int restant = solde.restante();
                double ratio = solde.prevue() == 0 ? 0 : (double) solde.consommee() / solde.prevue();
                String nomPresta = ligne.getPrestation().getNom();
                if (restant <= 0) {
                    alertes.add(new AlerteDTO("danger",
                            nomPresta + " — solde épuisé sur " + ordre.getNumero() + " (" + ordre.getMarche().getReference() + ")"));
                } else if (ratio >= SEUIL_ALERTE_QUANTITE_FAIBLE) {
                    alertes.add(new AlerteDTO("warn",
                            nomPresta + " — " + restant + " unités restantes sur " + ordre.getNumero()));
                }
            }
        }

        for (Reglement r : reglementRepository.findAll()) {
            double regle = r.getMontantRegle() == null ? 0 : r.getMontantRegle();
            if (regle < r.getMontantTTC()) {
                alertes.add(new AlerteDTO("warn",
                        "Facture " + r.getNumeroFacture() + " partiellement réglée (" + r.getMarche().getReference() + ")"));
            }
        }

        return alertes;
    }

    // ------------------------------------------------------------------
    // Vue consolidée "Commanditaires" (section 6)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<CommanditaireDTO> commanditaires() {
        Map<String, List<Marche>> parCommanditaire = marcheRepository.findAll().stream()
                .collect(Collectors.groupingBy(Marche::getCommanditaire, LinkedHashMap::new, Collectors.toList()));

        List<CommanditaireDTO> resultat = new ArrayList<>();
        for (Map.Entry<String, List<Marche>> entree : parCommanditaire.entrySet()) {
            List<ContratCommanditaireDTO> contrats = new ArrayList<>();
            double prevuTotal = 0, executeTotal = 0;
            for (Marche marche : entree.getValue()) {
                ContratCommanditaireDTO contrat = construireContrat(marche);
                contrats.add(contrat);
                prevuTotal += contrat.montantPrevu();
                executeTotal += contrat.montantExecute();
            }
            resultat.add(new CommanditaireDTO(entree.getKey(), contrats, prevuTotal, executeTotal));
        }
        return resultat;
    }

    private ContratCommanditaireDTO construireContrat(Marche marche) {
        List<OrdreCommande> ordres = ordreCommandeRepository.findByMarcheId(marche.getId());

        // Quantités total (inscrite au marché) / servie / restante, agrégées par NOM de prestation
        // — colonnes dynamiques de l'écran Commanditaires (une colonne par type de prestation présent).
        Map<String, int[]> parNom = new LinkedHashMap<>(); // [0] = total, [1] = servi
        for (Prestation prestation : marche.getPrestations()) {
            parNom.put(prestation.getNom(), new int[]{prestation.getQuantitePrevue(), 0});
        }
        for (OrdreCommande ordre : ordres) {
            Map<Long, ConsommationService.Solde> soldes = consommationService.calculerSoldesParPrestation(ordre);
            for (LigneOrdre ligne : ordre.getLignes()) {
                ConsommationService.Solde solde = soldes.get(ligne.getPrestation().getId());
                if (solde == null) continue;
                int[] agg = parNom.get(ligne.getPrestation().getNom());
                if (agg != null) agg[1] += solde.consommee();
            }
        }
        List<PrestationConsommationDTO> prestations = parNom.entrySet().stream()
                .map(e -> new PrestationConsommationDTO(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[0] - e.getValue()[1]))
                .toList();

        double montantPrevu = marche.getPrestations().stream()
                .mapToDouble(p -> p.getQuantitePrevue() * p.getPrixUnitaire())
                .sum();
        double montantExecute = ordres.stream().mapToDouble(consommationService::montantExecute).sum();

        List<Reglement> tousReglements = reglementRepository.findByMarcheId(marche.getId());
        double montantRegle = tousReglements.stream().mapToDouble(r -> r.getMontantRegle() == null ? 0 : r.getMontantRegle()).sum();

        // Détail dépliable : chaque ordre du marché avec les règlements qui s'y rapportent
        // (rapprochés soit par identifiant, soit par numéro saisi librement par le Comptable).
        List<OrdreCommandeResumeDTO> ordresResume = new ArrayList<>();
        for (OrdreCommande ordre : ordres) {
            List<Reglement> reglementsDeOrdre = tousReglements.stream()
                    .filter(r -> (r.getOrdreCommande() != null && r.getOrdreCommande().getId().equals(ordre.getId()))
                            || (r.getNumeroOrdreCommandeSaisi() != null
                                && r.getNumeroOrdreCommandeSaisi().equalsIgnoreCase(ordre.getNumero())))
                    .toList();
            double regleOrdre = reglementsDeOrdre.stream().mapToDouble(r -> r.getMontantRegle() == null ? 0 : r.getMontantRegle()).sum();
            double factureOrdre = reglementsDeOrdre.stream().mapToDouble(Reglement::getMontantTTC).sum();
            ordresResume.add(new OrdreCommandeResumeDTO(
                    ordre.getId(), ordre.getNumero(), ordre.getDate(), ordre.getStatut().name(),
                    consommationService.montantExecute(ordre), regleOrdre, factureOrdre,
                    reglementsDeOrdre.stream().map(this::versResumeReglement).toList()
            ));
        }

        // Règlements rattachés au marché entier (aucun ordre précis renseigné ni rapproché)
        List<ReglementResumeDTO> reglementsGlobaux = tousReglements.stream()
                .filter(r -> r.getOrdreCommande() == null
                        && (r.getNumeroOrdreCommandeSaisi() == null || r.getNumeroOrdreCommandeSaisi().isBlank()))
                .map(this::versResumeReglement)
                .toList();

        return new ContratCommanditaireDTO(
                marche.getId(), marche.getReference(), marche.getObjet(), marche.getStatut().name(),
                prestations, montantPrevu, montantExecute, montantRegle, montantPrevu - montantExecute,
                ordresResume, reglementsGlobaux
        );
    }

    private ReglementResumeDTO versResumeReglement(Reglement r) {
        return new ReglementResumeDTO(
                r.getId(), r.getNumeroFacture(), r.getDateFacture(), r.getMontantTTC(), r.getMontantRegle(),
                r.getModeReglement() != null ? r.getModeReglement().name() : null
        );
    }
}
