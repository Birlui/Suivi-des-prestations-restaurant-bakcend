package com.marches.restauration.dto;

import java.time.LocalDateTime;

public record ErreurResponse(
        LocalDateTime horodatage,
        int statut,
        String erreur,
        String message,
        String chemin
) {}
