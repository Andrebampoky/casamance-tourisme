# Référence API — Casamance Tourisme

Toutes les routes utilisent le préfixe `/api`. Les corps et réponses sont JSON, sauf les QR Codes (SVG dans un data URI). Les références de réservation sont des UUID aléatoires servant de jetons bearer : ne les publiez pas. Les routes `/api/admin/**` exigent `X-Admin-Token` configuré par `RESERVATION_ADMIN_TOKEN`.

## Visites et réservations

| Méthode | Route | Usage | Accès |
|---|---|---|---|
| GET | `/visits?q=&category=&date=` | Rechercher les visites | Public |
| GET | `/visits/{id}` | Détail d’une visite | Public |
| POST | `/bookings` | Créer une demande en attente | Public |
| GET | `/bookings/{reference}` | Lire un récapitulatif | Référence bearer |
| POST | `/bookings/{reference}/cancel` | Annuler selon la règle métier | Référence bearer |
| GET | `/admin/bookings?status=PENDING` | Lister les demandes | Admin |
| POST | `/admin/bookings/{reference}/confirm` | Confirmer et créer billet/outbox | Admin |

Exemple de création : `{"visitId":1,"customerName":"Awa","email":"awa@example.sn","visitDate":"2026-12-01","travelers":2}`. La date doit être actuelle ou future, le nombre de voyageurs doit respecter les validations métier et la capacité est contrôlée côté serveur.

## Billets

| Méthode | Route | Usage |
|---|---|---|
| GET | `/tickets/{reference}` | Lire le billet et son QR SVG |
| GET | `/tickets/verify/{token}` | Vérifier un billet sans exposer les coordonnées |

Le QR contient une URL de vérification et un jeton distinct de la référence de réservation. Une réservation annulée invalide le billet.

## Itinéraires

| Méthode | Route | Usage |
|---|---|---|
| GET | `/itineraries/interests` | Catalogue des centres d’intérêt |
| POST | `/itineraries` | Générer et enregistrer un parcours |
| GET | `/itineraries/{reference}` | Relire un parcours |

Exemple : `{"interests":["nature","culture"],"startDate":"2026-12-01","durationDays":3,"travelers":2,"budget":60000}`. Le budget est en FCFA. Le parcours est calculé depuis le catalogue; transport et distances ne sont pas estimés.

## Événements

| Méthode | Route | Usage | Accès |
|---|---|---|---|
| GET | `/events?type=&date=&from=&to=&location=&q=` | Recherche avec filtres combinables | Public |
| POST | `/admin/events` | Publier un événement | Admin |

## Satisfaction

| Méthode | Route | Usage | Accès |
|---|---|---|---|
| GET | `/satisfaction/reservations/{reference}/questions` | Obtenir les questions après visite | Référence bearer |
| POST | `/satisfaction/reservations/{reference}` | Soumettre une réponse unique | Référence bearer |
| GET | `/admin/satisfaction/questions` | Lister les questions | Admin |
| POST | `/admin/satisfaction/questions` | Ajouter une question | Admin |
| PATCH | `/admin/satisfaction/questions/{id}/active?active=true` | Activer/désactiver | Admin |
| GET | `/admin/satisfaction/statistics?visitId=&from=&to=` | Statistiques par activité et période | Admin |

Les valeurs invalides, réponses manquantes, doubles réponses ou enquêtes avant la fin de la visite sont rejetées par l’API.

## Statuts et erreurs

La réservation suit `PENDING → CONFIRMED → COMPLETED`; `CANCELLED` est la transition d’annulation autorisée par la règle courante. Les erreurs métier utilisent les statuts HTTP adaptés (`400`, `403`, `404`, `409`). Les DTO de réservation ne renvoient pas le nom ni l’adresse e-mail.
