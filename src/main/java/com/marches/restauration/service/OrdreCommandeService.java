package com.marches.restauration.service;

import com.marches.restauration.domain.LigneOrdre;
import com.marches.restauration.domain.Marche;
import com.marches.restauration.domain.OrdreCommande;
import com.marches.restauration.domain.Prestation;
import com.marches.restauration.domain.enums.StatutOrdreCommande;
import com.marches.restauration.dto.LigneOrdreRequest;
import com.marches.restauration.dto.LigneOrdreResponse;
import com.marches.restauration.dto.OrdreCommandeRequest;
import com.marches.restauration.dto.OrdreCommandeResponse;
import com.marches.restauration.exception.RessourceNonTrouveeException;
import com.marches.restauration.repository.OrdreCommandeRepository;
import com.marches.restauration.repository.PrestationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * L'ORDRE DE COMMANDE découle toujours d'un marché. Son numéro est
 * généré automatiquement au format OC-AAAA-XXX (règle de gestion §5) :
 * l'utilisateur ne le saisit jamais.
 */
@Service
@RequiredArgsConstructor
public class OrdreCommandeService {

    private static final Pattern SUFFIXE_NUMERIQUE = Pattern.compile("(\\d+)$");

    private final OrdreCommandeRepository ordreCommandeRepository;
    private final PrestationRepository prestationRepository;
    private final MarcheService marcheService;
    private final ConsommationService consommationService;
    private final AuditService auditService;

    @Transactional
    public OrdreCommandeResponse creer(OrdreCommandeRequest requete, String auteur) {
        Marche marche = marcheService.trouver(requete.marcheId());

        OrdreCommande ordre = OrdreCommande.builder()
                .numero(genererProchainNumero())
                .date(requete.date())
                .marche(marche)
                .statut(StatutOrdreCommande.ACTIF)
                .build();

        for (LigneOrdreRequest ligneRequete : requete.lignes()) {
            Prestation prestation = prestationRepository.findById(ligneRequete.prestationId())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Prestation introuvable : " + ligneRequete.prestationId()));
            if (!prestation.getMarche().getId().equals(marche.getId())) {
                throw new IllegalArgumentException(
                        "La prestation \"" + prestation.getNom() + "\" n'appartient pas au marché sélectionné.");
            }
            ordre.ajouterLigne(LigneOrdre.builder()
                    .prestation(prestation)
                    .quantitePrevue(ligneRequete.quantitePrevue())
                    .build());
        }

        ordre = ordreCommandeRepository.save(ordre);
        auditService.journaliser(auteur, "CREATION", "ORDRE_COMMANDE", ordre.getNumero(),
                "Marché " + marche.getReference());
        return vers(ordre);
    }

    /**
     * Reprend la suite du plus grand numéro déjà attribué, tous marchés confondus
     * (ex. dernier numéro OC-2026-046 -> prochain OC-2026-047), exactement comme
     * dans le prototype front-end.
     */
    private String genererProchainNumero() {
        int annee = LocalDate.now().getYear();
        int max = 0;
        for (String numero : ordreCommandeRepository.findAllNumeros()) {
            Matcher matcher = SUFFIXE_NUMERIQUE.matcher(numero == null ? "" : numero);
            if (matcher.find()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }
        return "OC-" + annee + "-" + String.format("%03d", max + 1);
    }

    @Transactional(readOnly = true)
    public List<OrdreCommandeResponse> lister(Long marcheId) {
        List<OrdreCommande> ordres = marcheId != null
                ? ordreCommandeRepository.findByMarcheId(marcheId)
                : ordreCommandeRepository.findAll();
        return ordres.stream().map(this::vers).toList();
    }

    @Transactional(readOnly = true)
    public OrdreCommandeResponse obtenir(Long id) {
        return vers(trouver(id));
    }

    public OrdreCommande trouver(Long id) {
        return ordreCommandeRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Ordre de commande introuvable : " + id));
    }

    public OrdreCommandeResponse vers(OrdreCommande ordre) {
        Map<Long, ConsommationService.Solde> soldes = consommationService.calculerSoldesParPrestation(ordre);

        List<LigneOrdreResponse> lignes = ordre.getLignes().stream().map(ligne -> {
            ConsommationService.Solde solde = soldes.get(ligne.getPrestation().getId());
            int consommee = solde != null ? solde.consommee() : 0;
            int restante = solde != null ? solde.restante() : ligne.getQuantitePrevue();
            return new LigneOrdreResponse(
                    ligne.getPrestation().getId(), ligne.getPrestation().getNom(), ligne.getPrestation().getPrixUnitaire(),
                    ligne.getQuantitePrevue(), consommee, restante
            );
        }).toList();

        double montantHT = 0, montantTVA = 0;
        for (LigneOrdre ligne : ordre.getLignes()) {
            double baseHT = ligne.getQuantitePrevue() * ligne.getPrestation().getPrixUnitaire();
            montantHT += baseHT;
            montantTVA += baseHT * (ligne.getPrestation().getTauxTVA() / 100.0);
        }

        return new OrdreCommandeResponse(
                ordre.getId(), ordre.getNumero(), ordre.getDate(),
                ordre.getMarche().getId(), ordre.getMarche().getReference(),
                ordre.getStatut(), lignes, montantHT, montantTVA, montantHT + montantTVA
        );
    }
}
