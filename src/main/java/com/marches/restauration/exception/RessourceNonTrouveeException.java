package com.marches.restauration.exception;

/** Levée lorsqu'une entité demandée (marché, ordre, commande, règlement...) n'existe pas. */
public class RessourceNonTrouveeException extends RuntimeException {
    public RessourceNonTrouveeException(String message) {
        super(message);
    }
}
