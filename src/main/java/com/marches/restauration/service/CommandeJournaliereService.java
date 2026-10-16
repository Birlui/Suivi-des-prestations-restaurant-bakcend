package com.marches.restauration.service;

import com.marches.restauration.domain.CommandeJournaliere;
import com.marches.restauration.domain.LigneCommande;
import com.marches.restauration.domain.OrdreCommande;
import com.marches.restauration.domain.Prestation;
import com.marches.restauration.domain.enums.StatutCommandeJournaliere;
import com.marches.restauration.dto.CommandeJournaliereModificationRequest;
import com.marches.restauration.dto.CommandeJournaliereRequest;
import com.marches.restauration.dto.CommandeJournaliereResponse;
import com.marches.restauration.dto.LigneCommandeRequest;
import com.marches.restauration.dto.LigneCommandeResponse;
import com.marches.restauration.exception.RessourceNonTrouveeException;
import com.marches.restauration.exception.SoldeDepasseException;
import com.marches.restauration.repository.CommandeJournaliereRepository;
import com.marches.restauration.repository.PrestationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Règle de gestion centrale : une commande journalière ne peut JAMAIS dépasser
 * le solde disponible (quantité prévue − quantité déjà consommée) de l'ordre de
 * commande correspondant. Pour une saisie sur une plage de dates (activité
 * pluri-journalière), le contrôle porte sur le CUMUL de tous les jours de la
 * période — si le cumul dépasse le solde, la création de l'ensemble des
 * commandes de la période est refusée (rien n'est enregistré).
 */
@Service
@RequiredArgsConstructor
public class CommandeJournaliereService {

    private final CommandeJournaliereRepository commandeRepository;
    private final PrestationRepository prestationRepository;
    private final OrdreCommandeService ordreCommandeService;
    private final ConsommationService consommationService;
    private final AuditService auditService;

    @Transactional
    public List<CommandeJournaliereResponse> creer(CommandeJournaliereRequest requete, String auteur) {
        OrdreCommande ordre = ordreCommandeService.trouver(requete.ordreId());

        List<LocalDate> dates = requete.estSurPeriode()
                ? datesEntre(requete.dateDebut(), requete.dateFin())
                : List.of(requete.date() != null ? requete.date() : LocalDate.now());

        if (dates.isEmpty()) {
            throw new IllegalArgumentException("La date de fin doit être postérieure ou égale à la date de début.");
        }

        Map<Long, ConsommationService.Solde> soldes = consommationService.calculerSoldesParPrestation(ordre);
        Map<Long, Integer> projection = new HashMap<>();
        for (LigneCommandeRequest ligne : requete.lignes()) {
            ConsommationService.Solde solde = soldes.get(ligne.prestationId());
            projection.put(ligne.prestationId(), solde != null ? solde.consommee() : 0);
        }

        // Vérification cumulative du solde sur l'ensemble de la période choisie (§5)
        for (LocalDate date : dates) {
            for (LigneCommandeRequest ligne : requete.lignes()) {
                ConsommationService.Solde solde = soldes.get(ligne.prestationId());
                int totalApres = projection.getOrDefault(ligne.prestationId(), 0) + ligne.quantite();
                if (solde != null && totalApres > solde.prevue()) {
                    Prestation prestation = prestationRepository.findById(ligne.prestationId())
                            .orElseThrow(() -> new RessourceNonTrouveeException("Prestation introuvable : " + ligne.prestationId()));
                    String periode = dates.size() > 1
                            ? String.format(" sur la période sélectionnée (%d jours, du %s au %s).",
                                    dates.size(), dates.get(0), dates.get(dates.size() - 1))
                            : ".";
                    throw new SoldeDepasseException(
                            "Quantité refusée : dépasse le solde disponible pour " + prestation.getNom() + periode);
                }
                projection.put(ligne.prestationId(), totalApres);
            }
        }

        String lotPeriodeId = dates.size() > 1 ? UUID.randomUUID().toString() : null;
        List<CommandeJournaliere> creees = new ArrayList<>();
        for (LocalDate date : dates) {
            CommandeJournaliere commande = CommandeJournaliere.builder()
                    .date(date)
                    .ordreCommande(ordre)
                    .lieuLivraison(requete.lieuLivraison())
                    .statut(StatutCommandeJournaliere.ACTIVE)
                    .saisiPar(auteur)
                    .lotPeriodeId(lotPeriodeId)
                    .build();
            for (LigneCommandeRequest ligne : requete.lignes()) {
                Prestation prestation = prestationRepository.findById(ligne.prestationId())
                        .orElseThrow(() -> new RessourceNonTrouveeException("Prestation introuvable : " + ligne.prestationId()));
                commande.ajouterLigne(LigneCommande.builder().prestation(prestation).quantite(ligne.quantite()).build());
            }
            creees.add(commandeRepository.save(commande));
        }

        String periodeTxt = dates.size() > 1
                ? dates.size() + " commandes du " + dates.get(0) + " au " + dates.get(dates.size() - 1)
                : "commande du " + dates.get(0);
        auditService.journaliser(auteur, "CREATION", "COMMANDE_JOURNALIERE", ordre.getNumero(), periodeTxt);

        return creees.stream().map(c -> vers(c, false)).toList();
    }

    private List<LocalDate> datesEntre(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null || fin.isBefore(debut)) return List.of();
        List<LocalDate> resultat = new ArrayList<>();
        LocalDate courante = debut;
        while (!courante.isAfter(fin)) {
            resultat.add(courante);
            courante = courante.plusDays(1);
        }
        return resultat;
    }

    /** Réservé à l'Administrateur (contrôle du rôle fait au niveau du contrôleur / de la sécurité). */
    @Transactional
    public CommandeJournaliereResponse modifier(Long id, CommandeJournaliereModificationRequest requete, String auteur) {
        CommandeJournaliere commande = trouver(id);
        OrdreCommande ordre = commande.getOrdreCommande();
        Map<Long, ConsommationService.Solde> soldes = consommationService.calculerSoldesParPrestation(ordre);

        for (LigneCommandeRequest ligne : requete.lignes()) {
            ConsommationService.Solde solde = soldes.get(ligne.prestationId());
            int propre = consommationService.consommeeParCommande(commande, ligne.prestationId());
            int consommeHorsCetteCommande = (solde != null ? solde.consommee() : 0) - propre;
            if (solde != null && consommeHorsCetteCommande + ligne.quantite() > solde.prevue()) {
                Prestation prestation = prestationRepository.findById(ligne.prestationId())
                        .orElseThrow(() -> new RessourceNonTrouveeException("Prestation introuvable : " + ligne.prestationId()));
                throw new SoldeDepasseException(
                        "Modification refusée : dépasse le solde disponible pour " + prestation.getNom() + ".");
            }
        }

        commande.setDate(requete.date());
        commande.setLieuLivraison(requete.lieuLivraison());
        commande.getLignes().clear();
        for (LigneCommandeRequest ligne : requete.lignes()) {
            Prestation prestation = prestationRepository.findById(ligne.prestationId())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Prestation introuvable : " + ligne.prestationId()));
            commande.ajouterLigne(LigneCommande.builder().prestation(prestation).quantite(ligne.quantite()).build());
        }

        auditService.journaliser(auteur, "MODIFICATION", "COMMANDE_JOURNALIERE", ordre.getNumero(),
                "Commande #" + id + " modifiée — le Cuisinier doit être informé du changement");
        return vers(commande, false);
    }

    /**
     * Réservé à l'Administrateur et au Comptable (le Gestionnaire qui a saisi la
     * commande ne peut pas l'annuler lui-même — règle de gestion §5). La commande
     * n'est jamais supprimée, seulement marquée ANNULÉE : elle reste visible mais
     * ne compte plus dans les quantités consommées ni les montants exécutés.
     */
    @Transactional
    public CommandeJournaliereResponse annuler(Long id, String auteur) {
        CommandeJournaliere commande = trouver(id);
        commande.setStatut(StatutCommandeJournaliere.ANNULEE);
        auditService.journaliser(auteur, "ANNULATION", "COMMANDE_JOURNALIERE",
                commande.getOrdreCommande().getNumero(), "Commande #" + id + " annulée");
        return vers(commande, false);
    }

    @Transactional(readOnly = true)
    public List<CommandeJournaliereResponse> lister(Long ordreId, LocalDate date, boolean masquerMontant) {
        List<CommandeJournaliere> commandes;
        if (ordreId != null) {
            commandes = commandeRepository.findByOrdreCommandeId(ordreId);
        } else if (date != null) {
            commandes = commandeRepository.findByDate(date);
        } else {
            commandes = commandeRepository.findAll();
        }
        return commandes.stream().map(c -> vers(c, masquerMontant)).toList();
    }

    @Transactional(readOnly = true)
    public CommandeJournaliereResponse obtenir(Long id, boolean masquerMontant) {
        return vers(trouver(id), masquerMontant);
    }

    CommandeJournaliere trouver(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Commande journalière introuvable : " + id));
    }

    /**
     * @param masquerMontant à {@code true} pour le rôle Cuisinier, qui n'a pas accès
     *                        aux données financières (section 4 du cahier des charges).
     */
    CommandeJournaliereResponse vers(CommandeJournaliere commande, boolean masquerMontant) {
        List<LigneCommandeResponse> lignes = commande.getLignes().stream().map(ligne -> new LigneCommandeResponse(
                ligne.getPrestation().getId(),
                ligne.getPrestation().getNom(),
                ligne.getQuantite(),
                masquerMontant ? null : ligne.getQuantite() * ligne.getPrestation().getPrixUnitaire()
        )).toList();

        Double montantTotal = masquerMontant ? null : lignes.stream()
                .mapToDouble(l -> l.montant() == null ? 0 : l.montant())
                .sum();

        OrdreCommande ordre = commande.getOrdreCommande();
        return new CommandeJournaliereResponse(
                commande.getId(), commande.getDate(),
                ordre.getId(), ordre.getNumero(), ordre.getMarche().getId(), ordre.getMarche().getReference(),
                commande.getLieuLivraison(), commande.getStatut(), lignes, montantTotal, commande.getLotPeriodeId()
        );
    }
}
