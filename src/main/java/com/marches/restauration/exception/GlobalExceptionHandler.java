package com.marches.restauration.exception;

import com.marches.restauration.dto.ErreurResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RessourceNonTrouveeException.class)
    public ResponseEntity<ErreurResponse> gererNonTrouve(RessourceNonTrouveeException ex, HttpServletRequest req) {
        return construire(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(SoldeDepasseException.class)
    public ResponseEntity<ErreurResponse> gererSoldeDepasse(SoldeDepasseException ex, HttpServletRequest req) {
        return construire(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(OperationNonAutoriseeException.class)
    public ResponseEntity<ErreurResponse> gererNonAutorise(OperationNonAutoriseeException ex, HttpServletRequest req) {
        return construire(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErreurResponse> gererAccesRefuse(AccessDeniedException ex, HttpServletRequest req) {
        return construire(HttpStatus.FORBIDDEN, "Accès refusé pour votre rôle.", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> gererValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construire(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErreurResponse> gererArgumentInvalide(IllegalArgumentException ex, HttpServletRequest req) {
        return construire(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    private ResponseEntity<ErreurResponse> construire(HttpStatus statut, String message, HttpServletRequest req) {
        ErreurResponse body = new ErreurResponse(
                LocalDateTime.now(), statut.value(), statut.getReasonPhrase(), message, req.getRequestURI()
        );
        return ResponseEntity.status(statut).body(body);
    }
}
