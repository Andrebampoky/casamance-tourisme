# Casamance Tourisme
## Application de réservation de visites touristiques en Casamance

**Rapport de projet — Licence 3 Informatique**  
Version pour soutenance · 17 novembre 2026

> Document établi à partir du dépôt et des vérifications effectivement réalisées. La pile Docker a été construite et démarrée ; les parcours et captures d’écran proviennent de l’application en exécution. Aucun envoi SMTP réel n’a été revendiqué, car aucun compte SMTP n’était configuré.

## 1. Introduction générale

Le tourisme en Casamance repose sur une offre variée d’activités culturelles, naturelles et littorales. Ce projet met en place une application web qui rassemble la découverte d’activités, la réservation, l’information avant visite et le retour de satisfaction. L’application est développée avec Angular, Spring Boot et PostgreSQL.

## 2. Contexte et problématique

Une offre distribuée sur plusieurs lieux rend difficile la comparaison des visites, la vérification des capacités et la centralisation des confirmations. La problématique est de fournir au visiteur un parcours numérique cohérent tout en donnant aux responsables des outils de gestion simples. Les données disponibles étant limitées, l’application distingue les données démonstratives des événements saisis par l’administration.

## 3. Analyse des besoins

Les besoins visiteurs sont : explorer le catalogue, consulter une activité, demander et suivre une réservation, obtenir un billet vérifiable après confirmation, préparer un séjour adapté à des intérêts/durée/budget, rechercher des événements et transmettre une enquête après visite. Les responsables doivent traiter les demandes, publier des événements et administrer le questionnaire. Les règles sensibles — statut, capacité, calcul du total et éligibilité à l’enquête — sont appliquées par l’API.

## 4. Spécifications fonctionnelles et non fonctionnelles

Fonctions : réservations et annulation conditionnelle ; cycle `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED` ; QR individuel vérifiable ; e-mail par outbox ; itinéraire déterministe ; recherche événementielle combinable ; questionnaire et agrégats. Contraintes : interface responsive, validation côté serveur, séparation des responsabilités, données persistées dans PostgreSQL et références non prévisibles. Les créneaux horaires, paiement, compte visiteur, géolocalisation routière et rôles complets ne sont pas dans cette version.

## 5. Architecture fonctionnelle

Le frontend Angular appelle une API Gateway Nginx. La Gateway route les réservations, billets, visites et enquêtes vers le Reservation Service ; les itinéraires et événements vont au Tourism Service. Le Notification Service traite les événements de réservation et l’envoi des courriels. Chaque service Spring Boot est déployable indépendamment et possède ses tables dans sa base PostgreSQL logique. Voir le diagramme [architecture.mmd](../uml/architecture.mmd).

## 6. Architecture technique

Angular 19 est servi par le conteneur frontend Nginx. L’API Gateway Nginx distribue `/api` sur le réseau privé Compose. Trois services Spring Boot 3.4/Java 17 utilisent chacun leur configuration et leur base logique PostgreSQL 16 : `casamance`, `casamance_experience` et `casamance_notifications`. En développement local, les URL des services sont configurables par variables d’environnement.

## 7. Justification des frontières microservices

La contrainte de livraison impose une architecture microservices. Les frontières suivent les besoins : Reservation possède catalogue, capacité, réservation, billet et satisfaction ; Tourism possède la planification des itinéraires et les événements ; Notification possède la livraison e-mail. Tourism appelle Reservation par HTTP au lieu de lire ses données. Reservation transmet les confirmations via une outbox transactionnelle et un relay HTTP avec reprise. Notification déduplique les événements et garde son outbox SMTP. Compose utilise un seul serveur PostgreSQL pour limiter les ressources locales, avec une base logique et des accès dédiés par service ; cette configuration n’équivaut pas à trois serveurs DB séparés.

## 8. Modélisation DDD

Le vocabulaire métier distingue visite, réservation, billet, notification, itinéraire, étape, événement, questionnaire et réponse. `Booking` est l’agrégat principal du cycle de réservation : il valide ses transitions et le montant est dérivé du tarif par participant. `BookingService` orchestre capacité et persistance ; le verrou pessimiste de la visite protège le contrôle de capacité concurrent. Les autres services applicatifs isolent les cas d’usage. Le domaine reste mêlé aux annotations JPA dans ce dépôt : il s’agit de DDD pragmatique, pas d’une architecture hexagonale pure.

## 9. Diagrammes UML

Le diagramme de classes [domain.mmd](../uml/domain.mmd) montre les associations et instantanés réellement persistés : visite-réservation, billet et enquête associés à une réservation, étapes embarquées dans un itinéraire. Le diagramme de séquence [reservation-sequence.mmd](../uml/reservation-sequence.mmd) décrit la création puis la confirmation. Aucun composant inexistant (Gateway, RabbitMQ, microservice autonome) n’y apparaît.

## 10. Modélisation des données

`Visit` contient le catalogue et sa capacité. `Booking` contient une référence UUID, une visite, date, participants, total, coordonnées nécessaires au traitement et état. `Ticket` porte un jeton distinct et un statut. `MailOutbox` suit destinataire, état, tentatives et prochaine reprise, avec référence unique. `Itinerary` enregistre contraintes et estimation ; ses `ItineraryStop` sont embarquées et mémorisent un instantané de visite. `TourismEvent` stocke les champs de recherche. `SatisfactionSurvey` est unique par réservation et contient les réponses embarquées ; celles-ci copient le libellé/type afin de conserver le sens historique. `SurveyQuestion` décrit les formulaires actifs.

## 11. Présentation des technologies

Angular structure l’interface. Spring Boot expose les contrats HTTP et isole les applications ; Spring Data JPA accède à la base propre à chaque service. PostgreSQL stocke les trois domaines dans des bases logiques séparées. Nginx fournit frontend et API Gateway. ZXing génère les QR. Spring Mail émet les notifications depuis l’outbox du Notification Service. Docker Compose orchestre les six composants applicatifs et la base partagée de développement.

## 12. Réalisation du frontend Angular

L’interface regroupe catalogue, détails, parcours de réservation, espace de suivi et pages billets, itinéraires, événements, satisfaction et administration. `ReservationApiService` centralise les appels HTTP et les modèles sont déclarés séparément. Les appels conteneurisés restent relatifs (`/api`) ; le proxy Nginx évite de coder une adresse localhost dans les requêtes du conteneur. `npm run build` a été exécuté avec succès. Aucun test unitaire Angular n’est configuré.

## 13. Réalisation du backend Spring Boot

Les contrôleurs traduisent les contrats HTTP en appels aux services. Les repositories Spring Data encapsulent l’accès à la base. Les requêtes sont validées par annotations et règles métier. Les DTO de réservation n’exposent pas le nom et l’adresse e-mail. Les routes administratives contrôlent `X-Admin-Token` par comparaison à une configuration serveur. Il s’agit d’un mécanisme de déploiement simplifié, non d’une authentification avec comptes et rôles.

## 14. Gestion des réservations

La demande contient activité, date, participants et coordonnées. Le serveur recalcule le total depuis le prix catalogue et verrouille la visite avant d’additionner les réservations non annulées de la date. Une demande commence en attente ; seul l’administrateur la confirme. L’annulation est possible avant la limite calendaire configurée. Une réservation confirmée devient terminée lors d’une lecture après sa date. Les tests de service couvrent calcul, capacité et transitions ; aucun test de concurrence sur PostgreSQL n’est exécuté.

## 15. Génération des tickets QR Code

La confirmation crée un ticket avec jeton UUID distinct de la référence de réservation. ZXing encode une URL de vérification, et non des coordonnées personnelles. Le visiteur lit son billet via la référence ; l’endpoint de vérification expose des informations minimales et le statut. L’annulation révoque le ticket. L’interface permet l’affichage et l’impression. Les tests unitaires vérifient la génération SVG et la vérification, sans scan matériel ni suite de tests navigateur intégrée au projet.

## 16. Confirmation par e-mail

Dans la même transaction que la confirmation, une notification unique est ajoutée à l’outbox. Le dispatcher traite l’outbox et Spring Mail envoie la référence, les détails utiles et l’accès au billet. Les erreurs sont journalisées avec reprises progressives jusqu’à cinq tentatives. SMTP absent laisse le message en attente. La livraison est au moins une fois : un doublon est possible après acceptation par SMTP si l’état envoyé ne peut ensuite être enregistré. Le serveur SMTP réel n’a pas été testé.

## 17. Génération des itinéraires

Le visiteur choisit plusieurs intérêts, date de début, durée (1–14 jours), participants et budget XOF/FCFA. Un algorithme déterministe sélectionne les activités correspondant aux catégories, capacité disponible et prix catalogue ; il limite à trois étapes et huit heures estimées par jour. Le total et le budget restant sont exposés, et l’itinéraire est enregistré puis consultable par référence. Aucun trajet routier ni distance n’est inventé. Modifier les critères produit un nouvel itinéraire ; l’édition d’un itinéraire existant n’est pas fournie.

## 18. Recherche des événements

La recherche combine type, date précise ou intervalle, lieu et mots-clés. Les mots-clés sont appliqués aux champs descriptifs pris en charge par le service. L’administration publie les informations et l’image associée. Aucun événement fictif n’est affiché comme réel. La modification, suppression, pagination et réservation d’événement restent à développer.

## 19. Enquêtes de satisfaction

Le questionnaire devient accessible pour une réservation terminée. Les types pris en charge sont note de 1 à 5, choix, oui/non et texte libre. Le backend valide les réponses et limite à une soumission par réservation. L’administration gère les questions, puis filtre les agrégats par activité et période ; les valeurs sont regroupées par mois et présentent moyenne des notes et distribution des réponses. Les données restent associées à une référence bearer, sans comptes visiteur. La confidentialité/anonymisation complète n’est donc pas garantie.

## 20. Dockerisation

Compose démarre PostgreSQL, les services Réservation, Tourisme et Notification, l’API Gateway et le frontend. Les services communiquent sur un réseau bridge privé ; seules la Gateway, l’interface et le port de développement PostgreSQL sont publiés. Les bases logiques sont initialisées par `database/init/01-databases.sql`. Si l’ancien volume est conservé, le script ne se rejoue pas : les deux bases ajoutées doivent alors être créées manuellement. Les healthchecks contrôlent les API de chaque service et leurs dépendances.

## 21. Tests et validation

Les tests unitaires sont lancés indépendamment pour chaque service avec `mvn -Dmaven.repo.local=/tmp/ms-reservation-m2 test -B`. Les résultats de la dernière exécution sont consignés après correction. `docker compose config` valide la configuration Compose. La validation de démarrage Docker et des appels interservices dépend de l’accès au démon Docker de la machine. Les captures décrivent les parcours UI précédemment exercés ; elles ne constituent pas un test automatisé de cette nouvelle topologie. Aucun SMTP réel ni test E2E automatisé n’est configuré.

## 22. Difficultés rencontrées

Le dépôt ne possédait que PostgreSQL dans Compose ; l’API et le frontend n’étaient pas dockerisés. Le proxy de développement Angular ciblait localhost, une adresse impropre à la communication depuis un conteneur. Nginx et le réseau Compose règlent ce point. Les ports 8080 et 4200 étaient occupés sur la machine et les ports hôte ont été ajustés respectivement à 8081 et 4201. Le premier build a aussi mis en évidence l’importance d’exclure `node_modules` et `target` des contextes Docker.

## 23. Limites actuelles

Le modèle n’a pas de créneaux ; les capacités sont journalières. Le compte visiteur et les rôles n’existent pas. La règle d’annulation est provisoire. La migration PostgreSQL repose encore sur `ddl-auto:update`. SMTP peut produire un doublon exceptionnel. Les itinéraires n’intègrent ni distances, ni transport, ni logement/repas. Les événements nécessitent une saisie réelle. L’image des visites est illustrative. Les tests d’intégration PostgreSQL concurrents, la suite navigateur automatisée et l’intégration SMTP réelle restent à ajouter.

## 24. Perspectives

Priorités : migrations Flyway, comptes et Spring Security, politique d’annulation validée avec prestataires, plages/horaires et test de charge concurrent, tests E2E automatisés, boîte SMTP de test et observabilité, gestion complète des événements et centres d’intérêt, édition d’itinéraire, données géographiques vérifiées, sauvegardes et politique de conservation des réponses.

## 25. Conclusion

Le projet est désormais organisé en microservices selon les responsabilités demandées : réservation, expériences touristiques et notification, avec une passerelle API et des bases logiques isolées. Les contrats HTTP et l’outbox définissent les échanges sans accès inter-base. Les limites restantes portent notamment sur l’exploitation distribuée, les migrations versionnées, la sécurité interservices, les tests E2E et l’envoi SMTP réel.

## 26. Bibliographie

1. Angular, documentation officielle : https://angular.dev/docs (consultée le 17 novembre 2026).
2. Spring Boot, documentation de référence : https://docs.spring.io/spring-boot/reference/ (consultée le 17 novembre 2026).
3. PostgreSQL, documentation version 16 : https://www.postgresql.org/docs/16/ (consultée le 17 novembre 2026).
4. Docker, contrôle de l’ordre de démarrage et des healthchecks Compose : https://docs.docker.com/compose/how-tos/startup-order/ (consultée le 17 novembre 2026).
5. ZXing, projet de génération/lecture de codes-barres et QR : https://github.com/zxing/zxing (consultée le 17 novembre 2026).
6. Sujet fourni : « Sujet 3 — Développement d’un système de réservation de visites touristiques », document inclus dans le dépôt.

## 27. Annexes

### A. Commandes de livraison

```bash
cp .env.example .env
# Remplacer les secrets d'exemple dans .env
docker compose config
docker compose up --build
```

Pour vérifier les tests : `cd services/reservation-service && mvn test
cd services/tourism-service && mvn test
cd services/notification-service && mvn test`; build interface : `cd frontend && npm run build`. Les 13 captures issues de l’application réellement démarrée sont dans `docs/captures/`; leur manifeste précise les fixtures utilisées. La référence des routes est dans [docs/api.md](../api.md), et les diagrammes sources sont sous `docs/uml/`.

### B. Principaux endpoints

Visites et réservations : `/api/visits`, `/api/bookings`, `/api/admin/bookings`. Billets : `/api/tickets/{reference}`, `/api/tickets/verify/{token}`. Itinéraires : `/api/itineraries/interests`, `/api/itineraries`. Événements : `/api/events`, `/api/admin/events`. Satisfaction : `/api/satisfaction/**`, `/api/admin/satisfaction/**`.
