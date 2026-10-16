package com.marches.restauration.exception;

/** Levée pour une opération interdite par les règles de gestion (ex. annulation par un rôle non habilité). */
public class OperationNonAutoriseeException extends RuntimeException {
    public OperationNonAutoriseeException(String message) {
        super(message);
    }
}
