package com.marches.restauration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application de gestion et de suivi des marchés de restauration.
 * <p>
 * Cette API REST reprend fidèlement le modèle de données et les règles de gestion
 * définis dans le cahier des charges technique (v1.1) et implémentés dans le
 * prototype front-end : Marché → Ordre de commande → Commande journalière,
 * avec contrôle strict des quantités et montants, et vue consolidée par Commanditaire.
 */
@SpringBootApplication
public class RestaurationMarchesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurationMarchesApplication.class, args);
    }
}
