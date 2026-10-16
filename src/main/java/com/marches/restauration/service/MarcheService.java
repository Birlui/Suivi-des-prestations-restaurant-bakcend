package com.marches.restauration.service;

import com.marches.restauration.domain.Marche;
import com.marches.restauration.domain.Prestation;
import com.marches.restauration.domain.enums.StatutMarche;
import com.marches.restauration.dto.MarcheRequest;
import com.marches.restauration.dto.MarcheResponse;
import com.marches.restauration.dto.PrestationRequest;
import com.marches.restauration.dto.PrestationResponse;
import com.marches.restauration.exception.RessourceNonTrouveeException;
import com.marches.restauration.repository.MarcheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcheService {

    private final MarcheRepository marcheRepository;
    private final AuditService auditService;

    @Transactional
    public MarcheResponse creer(MarcheRequest requete, String auteur) {
        if (marcheRepository.existsByReference(requete.reference())) {
            throw new IllegalArgumentException(
                    "La référence " + requete.reference() + " est déjà utilisée par un autre marché.");
        }

        Marche marche = Marche.builder()
                .reference(requete.reference())
                .objet(requete.objet())
                .commanditaire(requete.commanditaire())
                .dateDebut(requete.dateDebut())
                .dateFin(requete.dateFin())
                .tauxTVA(requete.tauxTVA())
                .documentMarche(requete.documentMarche())
                .infosFiscales(requete.infosFiscales())
                // Un marché nouvellement créé est directement "En cours" (comme dans le prototype) ;
                // le statut "En préparation" reste disponible pour un usage ultérieur (brouillon).
                .statut(StatutMarche.EN_COURS)
                .build();

        for (PrestationRequest prestationRequest : requete.prestations()) {
            marche.ajouterPrestation(Prestation.builder()
                    .nom(prestationRequest.nom())
                    .prixUnitaire(prestationRequest.prixUnitaire())
                    .quantitePrevue(prestationRequest.quantitePrevue())
                    .tauxTVA(prestationRequest.tauxTVA())
                    .build());
        }

        marche = marcheRepository.save(marche);
        auditService.journaliser(auteur, "CREATION", "MARCHE", marche.getReference(), requete.objet());
        return vers(marche);
    }

    @Transactional(readOnly = true)
    public List<MarcheResponse> lister() {
        return marcheRepository.findAll().stream().map(this::vers).toList();
    }

    @Transactional(readOnly = true)
    public MarcheResponse obtenir(Long id) {
        return vers(trouver(id));
    }

    public Marche trouver(Long id) {
        return marcheRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Marché introuvable : " + id));
    }

    /**
     * Un marché ne peut jamais être supprimé, uniquement clôturé (règle de gestion §5).
     */
    @Transactional
    public MarcheResponse cloturer(Long id, String auteur) {
        Marche marche = trouver(id);
        marche.setStatut(StatutMarche.CLOTURE);
        auditService.journaliser(auteur, "MODIFICATION", "MARCHE", marche.getReference(), "Clôture du marché");
        return vers(marche);
    }

    public MarcheResponse vers(Marche marche) {
        List<PrestationResponse> prestations = marche.getPrestations().stream()
                .map(p -> new PrestationResponse(p.getId(), p.getNom(), p.getPrixUnitaire(), p.getQuantitePrevue(), p.getTauxTVA()))
                .toList();
        return new MarcheResponse(
                marche.getId(), marche.getReference(), marche.getObjet(), marche.getCommanditaire(),
                marche.getDateDebut(), marche.getDateFin(), marche.getTauxTVA(), marche.getStatut(),
                marche.getDocumentMarche(), marche.getInfosFiscales(), prestations
        );
    }
}
