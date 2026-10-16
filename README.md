# Backend — Gestion et suivi des marchés de restauration

API REST en **Java / Spring Boot** qui implémente fidèlement le modèle de données
et les règles de gestion du prototype front-end et du cahier des charges
technique (v1.1) : Marché → Ordre de commande → Commande journalière →
Facturation → Règlement, avec contrôle strict des quantités/montants et une
vue consolidée par Commanditaire.

## Sommaire
1. [Stack technique](#stack-technique)
2. [Démarrage rapide](#démarrage-rapide)
3. [Comptes de démonstration](#comptes-de-démonstration)
4. [Modèle de données](#modèle-de-données)
5. [Règles de gestion implémentées](#règles-de-gestion-implémentées)
6. [Rôles et permissions](#rôles-et-permissions)
7. [Référence de l'API](#référence-de-lapi)
8. [Passage en production](#passage-en-production)
9. [Limites connues / suite possible](#limites-connues--suite-possible)

---

## Stack technique

| Composant          | Choix                                    |
|---------------------|-------------------------------------------|
| Langage             | Java 17                                   |
| Framework           | Spring Boot 3.3 (Web, Data JPA, Security, Validation) |
| Base de données démo | H2 en mémoire (aucune installation requise) |
| Base de données prod | PostgreSQL (profil `postgres` fourni)     |
| Sécurité             | HTTP Basic + rôles (`@PreAuthorize`), mots de passe BCrypt |
| Build                | Maven                                     |

## Démarrage rapide

Prérequis : **Java 17** et **Maven** installés (`java -version`, `mvn -version`).

```bash
cd backend
mvn spring-boot:run
```

L'API démarre sur `http://localhost:8080`. Les données de démonstration
(2 marchés, 2 ordres, 3 commandes, 2 règlements, 4 comptes) sont chargées
automatiquement au premier démarrage — voir `DataInitializer`.

Test rapide :
```bash
curl -u admin:admin123 http://localhost:8080/api/marches
```

La console H2 (pour inspecter la base en mémoire pendant le développement)
est disponible sur `http://localhost:8080/h2-console`
(JDBC URL : `jdbc:h2:mem:marches_restauration`, utilisateur `sa`, mot de passe vide).

> **Remarque sur cet environnement de génération** : le sandbox utilisé pour écrire
> ce code n'a pas accès à Maven Central, il n'a donc pas été possible d'exécuter
> `mvn compile` ici pour une vérification automatique. Le code a été relu
> attentivement (types, imports, annotations JPA/Lombok/Security), mais lancez
> `mvn clean verify` de votre côté avant toute mise en production, et signalez
> toute erreur de compilation resituelle — je la corrigerai immédiatement.

## Comptes de démonstration

| Identifiant     | Mot de passe        | Rôle                          |
|------------------|----------------------|-------------------------------|
| `admin`          | `admin123`           | Administrateur                |
| `comptable`      | `comptable123`       | Comptable                     |
| `gestionnaire`   | `gestionnaire123`    | Gestionnaire des commandes     |
| `cuisinier`      | `cuisinier123`       | Cuisinier                     |

⚠️ Mots de passe volontairement simples, **uniquement pour la démonstration**.
À remplacer avant toute mise en production (voir [Passage en production](#passage-en-production)).

## Modèle de données

Reprend exactement les entités du cahier des charges (section 3) :

- **Marche** — le contrat global (référence, objet, commanditaire, dates, statut).
- **Prestation** — toujours liée à un marché (nom, prix unitaire, quantité prévue, taux de TVA propres à ce marché).
- **OrdreCommande** — découle d'un marché ; `numero` est **généré par le serveur** (`OC-AAAA-XXX`), jamais saisi.
- **LigneOrdre** — quantité prévue par prestation, pour un ordre donné.
- **CommandeJournaliere** — consomme le solde d'un ordre, jour par jour ; porte `lieuLivraison` et, si elle a été créée sur une plage de dates, un `lotPeriodeId` commun.
- **LigneCommande** — prestation + quantité demandée ce jour-là.
- **Reglement** — rattaché à un marché, et optionnellement à un ordre de commande (`numeroOrdreCommandeSaisi` est un texte libre, rapproché automatiquement si possible).
- **Utilisateur** — identifiant, mot de passe (BCrypt), rôle.
- **JournalOperation** — trace auteur/date/action de chaque création, modification ou annulation.

## Règles de gestion implémentées

Toutes les règles de la section 5 du cahier des charges sont appliquées côté serveur (jamais seulement côté front-end) :

- Une commande journalière ne peut **jamais dépasser le solde disponible** (quantité prévue − consommée) d'une prestation sur son ordre. Pour une saisie sur **plage de dates**, le contrôle porte sur le **cumul** de tous les jours : si un seul jour dépasse le solde cumulé, **aucune** commande de la période n'est créée (`SoldeDepasseException` → HTTP 409).
- Modification d'une commande : le solde est recalculé **hors la consommation propre à cette commande** (elle ne se pénalise pas elle-même), réservé à l'Administrateur.
- Annulation : réservée à l'Administrateur et au Comptable — jamais au Gestionnaire qui a saisi la commande. Une commande annulée est conservée (jamais supprimée) et exclue des totaux.
- Un marché ou un ordre de commande ne sont **jamais supprimés**, uniquement clôturés/annulés.
- Le numéro d'ordre de commande est **toujours généré par le serveur** — la requête de création ne comporte pas ce champ.
- Un règlement peut être saisi avec un numéro d'ordre en texte libre ; s'il ne correspond à aucun ordre existant du marché, il est conservé tel quel et simplement marqué `rapprochementReussi: false` dans la réponse — jamais rejeté pour cette seule raison.
- Le prix unitaire et le taux de TVA sont propres à chaque **prestation d'un marché donné** (deux marchés peuvent facturer la même prestation différemment).
- Toute création/modification/annulation est journalisée (auteur, date, heure) dans `JournalOperation`.

## Rôles et permissions

| Rôle | Accès API |
|------|-----------|
| **Administrateur** | Tout : marchés, ordres, commandes (création + **modification** + annulation), règlements, commanditaires, tableau de bord |
| **Comptable** | Marchés (création), règlements (création), commanditaires, tableau de bord, **annulation** de commandes |
| **Gestionnaire des commandes** | Ordres (création), commandes journalières (création) |
| **Cuisinier** | Commandes journalières en **lecture seule**, limitées au jour courant par défaut, **sans les montants** (masqués dans la réponse JSON) |

La matrice complète est appliquée via `@PreAuthorize` sur chaque endpoint (voir les contrôleurs dans `web/`).

## Référence de l'API

Toutes les requêtes nécessitent l'authentification HTTP Basic (`-u identifiant:mot_de_passe`).

### Marchés
```bash
GET    /api/marches                       # Administrateur, Comptable
GET    /api/marches/{id}
POST   /api/marches                       # création avec ses prestations
PATCH  /api/marches/{id}/cloturer         # Administrateur uniquement
```
Exemple de création :
```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/marches \
  -H "Content-Type: application/json" \
  -d '{
    "reference": "MR-2026-030",
    "objet": "Restauration séminaire annuel",
    "commanditaire": "Chambre de Commerce",
    "dateDebut": "2026-09-01",
    "dateFin": "2026-09-05",
    "tauxTVA": 18,
    "prestations": [
      { "nom": "Pause-café", "prixUnitaire": 1500, "quantitePrevue": 200, "tauxTVA": 18 }
    ]
  }'
```

### Ordres de commande
```bash
GET    /api/ordres?marcheId=1             # Administrateur, Gestionnaire
GET    /api/ordres/{id}
POST   /api/ordres                        # numero généré automatiquement par le serveur
```
```bash
curl -u gestionnaire:gestionnaire123 -X POST http://localhost:8080/api/ordres \
  -H "Content-Type: application/json" \
  -d '{ "marcheId": 1, "date": "2026-09-01", "lignes": [ { "prestationId": 1, "quantitePrevue": 100 } ] }'
```

### Commandes journalières
```bash
GET    /api/commandes?ordreId=1&date=2026-08-10     # Administrateur, Gestionnaire, Cuisinier
POST   /api/commandes                                # Administrateur, Gestionnaire
PUT    /api/commandes/{id}                            # Administrateur uniquement
POST   /api/commandes/{id}/annuler                    # Administrateur, Comptable
```
Jour unique :
```bash
curl -u gestionnaire:gestionnaire123 -X POST http://localhost:8080/api/commandes \
  -H "Content-Type: application/json" \
  -d '{ "ordreId": 1, "date": "2026-08-13", "lieuLivraison": "Salle B",
        "lignes": [ { "prestationId": 1, "quantite": 40 } ] }'
```
Activité sur plusieurs jours (une commande générée par jour, cumul vérifié) :
```bash
curl -u gestionnaire:gestionnaire123 -X POST http://localhost:8080/api/commandes \
  -H "Content-Type: application/json" \
  -d '{ "ordreId": 1, "dateDebut": "2026-08-18", "dateFin": "2026-08-22",
        "lieuLivraison": "Salle B", "lignes": [ { "prestationId": 1, "quantite": 40 } ] }'
```

### Règlements
```bash
GET    /api/reglements?marcheId=1         # Administrateur, Comptable
POST   /api/reglements
```
```bash
curl -u comptable:comptable123 -X POST http://localhost:8080/api/reglements \
  -H "Content-Type: application/json" \
  -d '{ "marcheId": 1, "numeroOrdreCommande": "OC-2026-045",
        "numeroFacture": "FA-2026-110", "dateFacture": "2026-08-20",
        "montantHT": 100000, "montantTVA": 18000, "montantTTC": 118000,
        "montantRegle": 118000, "modeReglement": "VIREMENT" }'
```

### Commanditaires & tableau de bord
```bash
GET /api/commanditaires                   # Administrateur, Comptable
GET /api/tableau-de-bord                  # montants globaux + alertes
GET /api/tableau-de-bord/alertes
```

## Passage en production

1. **Base de données** : activer le profil PostgreSQL.
   ```bash
   export DB_URL=jdbc:postgresql://mon-serveur:5432/marches_restauration
   export DB_USER=...
   export DB_PASSWORD=...
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```
2. **Mots de passe** : supprimer/désactiver `DataInitializer` (ou le rendre conditionnel à un profil `dev`), créer les comptes réels via un script de migration.
3. **HTTPS** : toujours déployer derrière un reverse proxy TLS (nginx, ALB, etc.).
4. **CORS** : si le front-end est servi depuis une autre origine, ajouter une configuration CORS explicite dans `SecurityConfig` (non incluse ici car le prototype ne précise pas encore le domaine final).
5. **Fichiers joints** (`documentMarche`, `pieceJustificative`, ...) : actuellement de simples chemins/URL en base ; brancher un vrai stockage (S3, disque partagé) selon l'infrastructure retenue.
6. **Jetons** : pour un usage mobile ou par des clients tiers, remplacer HTTP Basic par OAuth2/JWT (la structure en `@PreAuthorize` par rôle reste inchangée).

## Limites connues / suite possible

- Pas encore d'export Excel/PDF (section 8 du cahier des charges) — à ajouter en Phase 2, probablement via une librairie comme Apache POI / OpenPDF, sur de nouveaux endpoints `/api/rapports/...`.
- Pas de pagination sur les listes (`GET /api/marches`, etc.) — à introduire si le volume de données le justifie.
- Le contrôle de rattachement d'un règlement à un ordre se fait par **numéro** (texte) : deux ordres de numéro identique dans des marchés différents ne posent pas de problème (le rapprochement est filtré par marché), mais un ordre "orphelin" recréé plus tard avec exactement le même numéro n'est pas un cas géré (peu probable, la numérotation étant strictement croissante).
