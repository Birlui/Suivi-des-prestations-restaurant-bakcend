package com.marches.restauration.exception;

/**
 * Levée lorsqu'une commande journalière (ou le cumul sur une plage de dates)
 * dépasserait le solde disponible d'une prestation sur son ordre de commande —
 * règle de gestion §5 : "Une commande journalière ne peut jamais dépasser le
 * solde disponible de l'ordre de commande correspondant."
 */
public class SoldeDepasseException extends RuntimeException {
    public SoldeDepasseException(String message) {
        super(message);
    }
}
