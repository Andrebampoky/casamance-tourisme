# Architecture microservices — Casamance Tourisme

## Vue d’ensemble

Le projet déploie trois services Spring Boot indépendants derrière une API Gateway Nginx. Le navigateur parle à la Gateway ; les communications internes utilisent le réseau privé Docker. Chaque service possède ses entités, repositories et une base PostgreSQL logique exclusive.

| Composant | Responsabilité | Base possédée | Échanges |
|---|---|---|---|
| Reservation Service | Catalogue, capacité, réservation, billets QR, satisfaction | `casamance` | Reçoit le trafic public routé ; publie les événements vers Notification |
| Tourism Service | Itinéraires, événements | `casamance_experience` | Consulte catalogue et disponibilité via l’API HTTP Réservation |
| Notification Service | Outbox e-mail, reprises et SMTP | `casamance_notifications` | Reçoit les événements internes Réservation |
| API Gateway | Routage `/api` | Aucune | Route réservations vers Reservation et itinéraires/événements vers Tourism |
| Frontend | Interface Angular | Aucune | Appelle la Gateway en même origine |

Pour le développement, un seul conteneur PostgreSQL héberge les trois bases afin d’alléger l’environnement. Il s’agit d’une isolation logique et de propriété des données, pas de trois serveurs PostgreSQL indépendants. Aucun service ne lit les tables d’un autre.

## Frontières métier

Réservation regroupe la capacité, le billet et la satisfaction car ils dépendent du même cycle de vie d’une réservation et doivent appliquer ses états. Tourism regroupe les itinéraires et les événements, qui sont des expériences distinctes mais s’appuient sur le catalogue par contrat HTTP. Notification possède le traitement e-mail et son état d’envoi ; il ne reçoit qu’un événement contenant les informations nécessaires.

## Intégration interservices

Tourism appelle `GET /api/visits` et `GET /api/visits/{id}/availability` sur Reservation. Il stocke dans son propre itinéraire un instantané de l’activité ; aucune clé étrangère inter-base n’est utilisée.

Lors d’une confirmation, Reservation écrit une entrée d’outbox dans la même transaction que l’état de réservation et le billet. Un relay envoie l’événement au Notification Service avec reprise progressive. Notification déduplique les confirmations par référence et écrit dans sa propre outbox avant l’envoi SMTP. L’annulation est transmise comme événement distinct. Le modèle est asynchrone, avec livraison au moins une fois ; les consommateurs doivent rester idempotents.

Cette taille de projet n’introduit pas de broker distribué. L’outbox + HTTP est plus simple à exploiter en local tout en évitant une transaction distribuée et les accès croisés aux bases. Pour des volumes ou exigences de disponibilité supérieurs, un broker et une stratégie de migration des schémas seraient des évolutions possibles.

## Règles clés

- Une réservation commence en `PENDING`; l’administration la confirme ou l’annule selon la règle métier.
- Le serveur calcule le total depuis le tarif courant et protège le contrôle de capacité par verrou de visite.
- Le billet QR porte un jeton distinct de la référence de réservation.
- Les coordonnées personnelles ne figurent pas dans la réponse publique de réservation.
- Une disponibilité d’itinéraire est demandée au service Réservation ; elle n’est pas calculée depuis une copie locale du stock.
- L’accès d’administration repose actuellement sur un jeton partagé ; aucun compte/role n’est configuré.

## Démarrage et ports

`docker compose up --build` lance PostgreSQL, les trois services, la Gateway et le frontend. L’API Gateway est publiée sur 8081, Angular sur 4201, PostgreSQL sur 5432. Sur un ancien volume, créez les bases `casamance_experience` et `casamance_notifications` manuellement ; le volume conserve la base `casamance` existante.

## Limites techniques

Les trois bases logiques partagent le même serveur physique Compose, les migrations sont encore gérées par `ddl-auto: update`, l’authentification admin est un secret partagé, et les tests E2E, de concurrence PostgreSQL et SMTP réel restent à mettre en place. L’outbox HTTP offre une intégration résiliente mais ne remplace pas la supervision, l’authentification interservices et une politique d’exploitation de production.
