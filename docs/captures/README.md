# Captures de l’application en exécution

Captures PNG réalisées avec Chrome headless le 17 novembre 2026 sur l’application Angular servie par le conteneur Nginx (`http://localhost:4201`). Les appels `/api` passaient par Nginx vers Spring Boot et PostgreSQL dans Compose. Les images sont des captures d’écran de l’application réellement exécutée, pas des maquettes.

| Fichier | Écran |
|---|---|
| `01-accueil.png` | Accueil et début du catalogue |
| `02-catalogue.png` | Catalogue complet |
| `03-detail-visite.png` | Détail d’une visite |
| `04-reservation-etape-1.png` | Réservation, activité choisie |
| `05-reservation-etape-2.png` | Réservation, date et coordonnées de recette |
| `06-reservation-recapitulatif.png` | Récapitulatif avant envoi |
| `07-confirmation-demande.png` | Confirmation d’enregistrement de la demande |
| `08-administration.png` | Console admin et gestion |
| `09-billet-qr.png` | Billet QR réel émis par l’API |
| `10-centres-interet.png` | Sélection des intérêts et paramètres de séjour |
| `11-itineraire-genere.png` | Itinéraire réellement généré et sauvegardé |
| `12-recherche-evenements.png` | Recherche et résultat événementiel de test |
| `13-questionnaire-satisfaction.png` | Questionnaire chargé depuis l’API |

Les réservations, réponses et l’événement utilisés pour les captures étaient des fixtures de recette (`example.test`, événement titré « Fixture de test technique »), sans données de touristes réels. Elles ont été supprimées de la base après les captures. L’événement temporaire apparaît dans la capture 12 afin de prouver l’affichage d’un résultat ; il ne constitue pas une annonce réelle. Le courriel SMTP n’a pas été envoyé : aucun serveur SMTP n’était configuré. L’enquête de la capture a été rendue disponible en plaçant la réservation de test à l’état terminé dans la base, pour ne pas attendre une date de visite future.
