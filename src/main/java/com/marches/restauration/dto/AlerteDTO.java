package com.marches.restauration.dto;

/** tone: "danger" (solde épuisé / rien réglé), "warn" (seuil 80% atteint / règlement partiel). */
public record AlerteDTO(String tone, String message) {}
