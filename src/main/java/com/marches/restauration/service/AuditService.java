package com.marches.restauration.service;

import com.marches.restauration.domain.JournalOperation;
import com.marches.restauration.repository.JournalOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Toute création, modification ou annulation est journalisée avec l'auteur,
 * la date et l'heure — règle de gestion §5 / architecture technique §9.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final JournalOperationRepository journalOperationRepository;

    public void journaliser(String auteur, String action, String entite, String entiteId, String details) {
        journalOperationRepository.save(JournalOperation.builder()
                .auteur(auteur)
                .action(action)
                .entite(entite)
                .entiteId(entiteId)
                .details(details)
                .build());
    }
}
