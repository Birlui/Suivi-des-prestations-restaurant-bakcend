package com.marches.restauration.service;

import com.marches.restauration.domain.Marche;
import com.marches.restauration.domain.OrdreCommande;
import com.marches.restauration.domain.Reglement;
import com.marches.restauration.dto.ReglementRequest;
import com.marches.restauration.dto.ReglementResponse;
import com.marches.restauration.repository.OrdreCommandeRepository;
import com.marches.restauration.repository.ReglementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Le Comptable saisit librement le numéro de l'ordre de commande concerné
 * (texte libre, avec suggestions côté client). Le système tente de le
 * rapprocher d'un ordre existant du même marché ; à défaut, le règlement
 * est conservé avec le texte saisi tel quel, marqué "non rapproché" — il
 * n'est jamais rejeté pour cette seule raison (voir dto.ReglementRequest).
 */
@Service
@RequiredArgsConstructor
public class ReglementService {

    private final ReglementRepository reglementRepository;
    private final OrdreCommandeRepository ordreCommandeRepository;
    private final MarcheService marcheService;
    private final AuditService auditService;

    @Transactional
    public ReglementResponse creer(ReglementRequest requete, String auteur) {
        Marche marche = marcheService.trouver(requete.marcheId());

        String saisie = requete.numeroOrdreCommande() == null ? "" : requete.numeroOrdreCommande().trim();
        OrdreCommande ordreTrouve = null;
        if (!saisie.isEmpty()) {
            ordreTrouve = ordreCommandeRepository
                    .findByNumeroIgnoreCaseAndMarcheId(saisie, marche.getId())
                    .orElse(null);
        }

        Reglement reglement = Reglement.builder()
                .marche(marche)
                .ordreCommande(ordreTrouve)
                .numeroOrdreCommandeSaisi(saisie)
                .numeroFacture(requete.numeroFacture())
                .dateFacture(requete.dateFacture())
                .montantHT(requete.montantHT())
                .montantTVA(requete.montantTVA())
                .montantTTC(requete.montantTTC())
                .montantRegle(requete.montantRegle() != null ? requete.montantRegle() : 0.0)
                .dateReglement(requete.dateReglement())
                .modeReglement(requete.modeReglement())
                .referencePaiement(requete.referencePaiement())
                .banqueCompte(requete.banqueCompte())
                .pieceJustificative(requete.pieceJustificative())
                .build();

        reglement = reglementRepository.save(reglement);
        auditService.journaliser(auteur, "CREATION", "REGLEMENT", requete.numeroFacture(),
                "Marché " + marche.getReference()
                        + (ordreTrouve != null ? " / Ordre " + ordreTrouve.getNumero() + " (rapproché)"
                           : saisie.isEmpty() ? " / facture globale" : " / numéro saisi non rapproché : " + saisie));
        return vers(reglement);
    }

    @Transactional(readOnly = true)
    public List<ReglementResponse> lister(Long marcheId) {
        List<Reglement> reglements = marcheId != null
                ? reglementRepository.findByMarcheId(marcheId)
                : reglementRepository.findAll();
        return reglements.stream().map(this::vers).toList();
    }

    ReglementResponse vers(Reglement r) {
        return new ReglementResponse(
                r.getId(), r.getMarche().getId(), r.getMarche().getReference(),
                r.getOrdreCommande() != null ? r.getOrdreCommande().getId() : null,
                r.getOrdreCommande() != null ? r.getOrdreCommande().getNumero() : null,
                r.getNumeroOrdreCommandeSaisi(),
                r.getOrdreCommande() != null,
                r.getNumeroFacture(), r.getDateFacture(),
                r.getMontantHT(), r.getMontantTVA(), r.getMontantTTC(), r.getMontantRegle(),
                r.getReste(), r.isSoldee(), r.getDateReglement(), r.getModeReglement(),
                r.getReferencePaiement(), r.getBanqueCompte()
        );
    }
}
