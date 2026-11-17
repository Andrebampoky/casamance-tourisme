# Casamance Tourisme

Application de découverte et de réservation touristique en Casamance, construite avec Angular, Java 17, Spring Boot, PostgreSQL et Docker Compose. L’architecture suit une approche microservices : trois applications Spring Boot autonomes possèdent leurs responsabilités, leur déploiement et leur base PostgreSQL logique. Une passerelle API Nginx expose les routes au frontend.

## Services et responsabilités

- **Reservation Service** (`services/reservation-service`) : catalogue/capacité, demandes de réservation, billets QR et satisfaction. Base `casamance`.
- **Tourism Service** (`services/tourism-service`) : itinéraires et événements. Base `casamance_experience`. Il consulte le catalogue et la disponibilité du Reservation Service par HTTP ; il ne lit pas ses tables.
- **Notification Service** (`services/notification-service`) : outbox e-mail et envoi SMTP avec reprises. Base `casamance_notifications`. Il ne lit pas les réservations.
- **API Gateway** (`gateway`) : routage public vers les services.
- **Frontend** (`frontend`) : application Angular servie par Nginx.

Compose utilise un serveur PostgreSQL commun pour limiter les ressources locales, mais chaque service se connecte exclusivement à sa propre base. Les bases ne sont pas partagées. Le réseau Compose est privé ; seuls le frontend, la passerelle et le port PostgreSQL de développement sont publiés sur l’hôte.

La confirmation de réservation écrit un événement dans l’outbox du service Réservation. Un relay HTTP réessaie son envoi vers le service Notification, qui déduplique par référence et garde sa propre outbox d’e-mail. Cette intégration évite un accès direct aux tables d’un autre service ; elle fournit une livraison au moins une fois. Les frontières, échanges et limites sont détaillés dans [l’architecture](docs/reservation-architecture.md) et le [diagramme](docs/uml/architecture.mmd).

## Fonctionnalités

- Catalogue, réservation avec contrôle de capacité, suivi et annulation conditionnelle.
- Confirmation administrateur, billet QR vérifiable et notification e-mail.
- Itinéraires selon centres d’intérêt, dates, durée et budget FCFA.
- Recherche et administration des événements.
- Questionnaire de satisfaction après visite.

## Démarrage Docker

Prérequis : Docker Engine et Docker Compose v2.

```bash
cp .env.example .env
# Remplacer les secrets d'exemple dans .env
docker compose config
docker compose up --build
```

Site : http://localhost:4201 ; API Gateway : http://localhost:8081 ; PostgreSQL : port hôte 5432. Les ports se règlent avec `WEB_PORT`, `API_PORT` et `DB_PORT`. Compose crée les bases des services dans un volume persistant lors de la première initialisation.

Arrêter sans supprimer les données : `docker compose down`. Supprimer aussi les bases locales : `docker compose down -v`.

Si le volume PostgreSQL existait déjà avant la migration, le script d’initialisation ne s’exécute pas une seconde fois. Créez les deux nouvelles bases une seule fois :

```bash
docker compose exec db psql -U casamance -d postgres -c 'CREATE DATABASE casamance_experience;'
docker compose exec db psql -U casamance -d postgres -c 'CREATE DATABASE casamance_notifications;'
```

Le service Réservation conserve la base `casamance` afin de préserver les réservations présentes dans le volume antérieur.

## Développement local

Chaque service se lance séparément avec Maven, Java 17 et une base PostgreSQL configurée pour lui. En local, les URL interservices sont configurables par `RESERVATION_SERVICE_URL` et `NOTIFICATION_SERVICE_URL`. Le frontend de développement se lance depuis `frontend` avec `npm ci && npm start`; son proxy Angular vise l’API de réservation locale, tandis que la pile complète et la répartition des routes s’utilisent via Docker Gateway.

Les tests backend sont séparés :

```bash
cd services/reservation-service && mvn test
cd services/tourism-service && mvn test
cd services/notification-service && mvn test
```

## Configuration

Voir [.env.example](.env.example). Les principales variables sont `POSTGRES_USER`, `DB_PASSWORD`, `RESERVATION_ADMIN_TOKEN`, les ports `API_PORT`/`WEB_PORT`/`DB_PORT`, et les variables `MAIL_*`. Laissez `MAIL_HOST` vide pour conserver les notifications en attente sans envoi SMTP. Ne versionnez jamais `.env` ni les identifiants SMTP.

## Documentation

- [Contrats API](docs/api.md)
- [Architecture et frontières métier](docs/reservation-architecture.md)
- [Rapport académique](docs/rapport/rapport.md), avec versions PDF/DOCX
- [Diagrammes UML](docs/uml/)
- [Captures de l’interface](docs/captures/)

Les captures d’interface documentent les parcours fonctionnels de l’application. Les tests unitaires couvrent des règles métier ; SMTP réel, tests PostgreSQL concurrents et tests E2E automatisés restent à valider.

## Publication GitHub

Le dépôt local est initialisé sur `main`; le compte communiqué est `AndreBampoky`. Aucun remote n’est configuré et GitHub CLI n’est pas connecté. Après connexion via `gh auth login`, définir l’identité Git, créer le commit initial, puis créer le dépôt distant choisi et pousser `main`. Le fichier `.env` est ignoré par Git.
