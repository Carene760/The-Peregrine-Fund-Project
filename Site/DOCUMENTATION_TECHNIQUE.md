# DOCUMENTATION TECHNIQUE

## Introduction

The Peregrine Fund App est une application de gestion d alertes terrain (principalement incendies) pour le suivi des messages, des statuts, des sites et des patrouilleurs.

Objectif principal:

- centraliser les signalements,
- suivre l evolution des alertes,
- offrir une interface web de consultation et de gestion,
- permettre la synchronisation avec une application mobile / gateway SMS.

Public cible actuel:

- developpeurs,
- utilisateurs finaux (agents, responsables, administrateurs).

---

## Architecture du systeme

### 1) Vue Fonctionnelle

Comportements attendus et services:

1. Authentification web utilisateur.
2. Consultation historique des alertes/messages.
3. Visualisation statistique par site et par type.
4. Gestion CRUD des entites principales (sites, patrouilleurs, messages, alertes, statuts, fonctions, users).
5. Reception de messages via API (`/api/**`) et traitement metier.
6. Synchronisation mobile (`/sync/**`) pour upload/download des donnees utiles.

Services metier principaux:

- SmsProcessingService
- AlerteService
- HistoriqueMessageStatusService
- MessageService
- SiteService
- UserService

### 2) Vue Technique / Organique

Composants logiciels:

- Backend Java Spring Boot
- Controllers Web (pages Thymeleaf)
- Controllers API REST (JSON)
- Services metier
- Repositories Spring Data JPA
- Base PostgreSQL
- Integration SendGrid (email)

Composants materiels / infrastructure (logique):

- Poste client web (navigateur)
- Terminal mobile / gateway SMS
- Serveur d application Spring Boot
- Serveur PostgreSQL

Dependances techniques (resume):

- Spring Boot Web
- Spring Data JPA
- Spring Security
- Thymeleaf
- PostgreSQL Driver
- SendGrid

Arborescence actuelle du code (navigation):

```text
Site/
  pom.xml
  src/main/
    java/com/example/serveur/
      ServeurApplication.java
      config/
        SecurityConfig.java
        SendGridConfig.java
      controller/
        AlerteController.java
        FonctionController.java
        HistoriqueMessageStatusController.java
        HistoriqueStatusAgentController.java
        InterventionController.java
        MessageController.java
        MessagePatrouilleurController.java
        PatrouilleurController.java
        ServeurController.java
        SiteController.java
        StatusAgentController.java
        StatusMessageController.java
        SyncController.java
        TypeAlerteController.java
        UserAppController.java
        UserController.java
        login/LoginController.java
        Historique/HistoriqueController.java
        Statistiques/StatistiqueController.java
      service/
        AlerteService.java
        AllowedNumbersService.java
        EmailService.java
        FonctionService.java
        HistoriqueMessageStatusService.java
        HistoriqueStatusAgentService.java
        InfoRetourService.java
        InterventionService.java
        MessagePatrouilleurService.java
        MessageService.java
        NiveauAlerteService.java
        PatrouilleurService.java
        SiteService.java
        SmsLoggingService.java
        SmsProcessingService.java
        SmsResponseService.java
        StatusAgentService.java
        StatusMessageService.java
        TypeAlerteService.java
        UserAppService.java
        UserService.java
      repository/
        AlerteRepository.java
        FonctionRepository.java
        FonctionZoneAlerteRepository.java
        HistoriqueMessageStatusRepository.java
        HistoriqueStatusAgentRepository.java
        InterventionRepository.java
        MessagePatrouilleurRepository.java
        MessageRepository.java
        PatrouilleursRepository.java
        SiteRepository.java
        StatusAgentRepository.java
        StatusMessageRepository.java
        TypeAlerteRepository.java
        UserAppRepository.java
        UserRepository.java
      model/
        Alerte.java
        Fonction.java
        FonctionZoneAlerte.java
        FonctionZoneAlerteId.java
        HistoriqueMessageStatus.java
        HistoriqueStatusAgent.java
        Intervention.java
        Message.java
        MessagePatrouilleur.java
        Patrouilleurs.java
        Serveur.java
        Site.java
        SmsRequest.java
        SmsResponse.java
        StatusAgent.java
        StatusMessage.java
        TypeAlerte.java
        User.java
        UserApp.java
      util/
        EncryptionUtil.java
    resources/
      application.properties
      templates/
        login.html
        historique.html
        statistiques.html
        agent.html
        User.html
      static/
        css/
        images/
```

### 3) Flux des donnees

Note compatibilite DOCX:

- cette section est prevue pour etre remplacee par des images (PNG/JPG) lisibles dans le document final.
- garder les schemas simples, contrastes forts, texte large.

[ESPACE RESERVE - IMAGE 1: FLUX GLOBAL DES DONNEES]

[ESPACE RESERVE - IMAGE 2: FLUX DETAILLE API MESSAGE-ALERTE]

[ESPACE RESERVE - IMAGE 3: FLUX DETAILLE API UPDATE-STATUS]

Reference textuelle (simple) en attendant les images:

1. Reception requete client -> controller.
2. Validation numero/type/format.
3. Traitement metier service.
4. Ecriture en base via repository.
5. Retour de reponse + journalisation.

---

## Documentation de l API

### A) Endpoints

| Domaine      | Methode  | Endpoint                  | Description                                     |
| ------------ | -------- | ------------------------- | ----------------------------------------------- |
| Auth Web     | GET      | /                         | Affiche la page de connexion                    |
| Auth Web     | POST     | /login                    | Verifie les identifiants web                    |
| Auth Web     | GET      | /logout                   | Deconnecte la session web                       |
| Historique   | GET      | /history                  | Affiche la page historique                      |
| Statistiques | GET      | /stat                     | Affiche la page statistiques                    |
| Sync         | POST     | /sync/upload              | Recoit une liste de messages mobile             |
| Sync         | GET      | /sync/download/{idUser}   | Retourne les messages d un utilisateur          |
| Sync         | GET      | /sync/historique/{idUser} | Retourne historique des statuts par utilisateur |
| Sync         | GET      | /sync/interventions       | Retourne les interventions disponibles          |
| Sync         | GET      | /sync/status              | Retourne la liste des statuts                   |
| API          | POST     | /api/webhook              | Recoit evenement sms:received                   |
| API          | POST     | /api/message              | Endpoint universel (webhook/direct)             |
| API          | POST     | /api/login                | Login mobile via message chiffre                |
| API          | POST     | /api/message-alerte       | Creation d alerte via message chiffre           |
| API          | POST     | /api/update-status        | Mise a jour de statut via message chiffre       |
| CRUD         | GET/POST | /alertes/**               | Gestion alertes                                 |
| CRUD         | GET/POST | /messages/**              | Gestion messages                                |
| CRUD         | GET/POST | /sites/**                 | Gestion sites                                   |
| CRUD         | GET/POST | /patrouilleurs/**         | Gestion patrouilleurs                           |
| CRUD         | GET/POST | /users/**                 | Gestion utilisateurs                            |
| CRUD         | GET/POST | /users-app/**             | Gestion comptes userapp                         |
| CRUD         | GET/POST | /status-messages/**       | Gestion statuts message                         |
| CRUD         | GET/POST | /status-agents/**         | Gestion statuts agent                           |
| CRUD         | GET/POST | /types-alerte/**          | Gestion types alerte                            |
| CRUD         | GET/POST | /fonctions/**             | Gestion fonctions                               |

### B) Exemples de requetes et reponses

#### 1. POST /api/message-alerte

Requete:

```json
{
  "phoneNumber": "+2613XXXXXXXX",
  "encryptedMessage": "...",
  "sendSmsResponse": true
}
```

Reponse succes:

```json
{
  "success": true,
  "message": "Alerte enregistree",
  "idSite": 2
}
```

Reponse erreur:

```json
{
  "success": false,
  "message": "Numero non autorise",
  "idSite": null
}
```

#### 2. POST /api/update-status

Requete:

```json
{
  "phoneNumber": "+2613XXXXXXXX",
  "encryptedMessage": "...",
  "sendSmsResponse": false
}
```

Reponse type:

```json
{
  "success": true,
  "message": "✅ Statut mis a jour"
}
```

#### 3. POST /api/login

Requete:

```json
{
  "phoneNumber": "+2613XXXXXXXX",
  "encryptedMessage": "...",
  "sendSmsResponse": true
}
```

Reponse type:

```json
{
  "success": true,
  "message": "Connexion reussie! ID: 5",
  "userId": "5"
}
```

#### 4. POST /sync/upload

Requete:

```json
[
  {
    "dateSignalement": "2026-01-03T11:15:13",
    "direction": "Est"
  }
]
```

Reponse type:

```text
Upload termine
```

#### 5. GET /sync/download/

Exemple:

```http
GET /sync/download/5
```

Reponse: liste JSON de messages.

#### 6. GET /sync/historique/

Exemple:

```http
GET /sync/historique/5
```

Reponse: liste JSON des historiques de statut.

#### 7. POST /login (web)

Requete formulaire:

```text
email=...&password=...
```

Reponse:

- succes: redirection vers /history
- echec: page login avec message d erreur

---

## Guides d Utilisation

### 1) Installation (developpeurs / technique)

Prerequis:

- JDK 17 ou plus
- Maven 3.9 ou plus
- PostgreSQL actif

Etapes:

1. Cloner le projet.
   - Lien github :
2. Creer la base tpf dans PostgreSQL.
3. Charger le schema puis les donnees.
   commande a executer dans le terminal : psql -U `<utilisateur_postgresql>`-d `<nom_base_de_donnee>` -f `<fichier>.sql`
4. Verifier la configuration application.
5. Lancer le serveur.

Commande de lancement:

```bash
mvn clean spring-boot:run
```

Acces local:

- http://localhost:8080

### 2) Fichiers necessaires pour le bon fonctionnement (configuration)

Fichiers critiques:

- pom.xml
- src/main/resources/application.properties
- src/main/java/com/example/serveur/config/SecurityConfig.java
- src/main/java/com/example/serveur/config/SendGridConfig.java
- src/main/java/com/example/serveur/util/EncryptionUtil.java

Fichiers base de donnees:

- ../bdd/structure_lisible.sql
- ../bdd/data_lisible.sql

Fichiers interface web:

- src/main/resources/templates/login.html
- src/main/resources/templates/historique.html
- src/main/resources/templates/statistiques.html
- src/main/resources/templates/agent.html
- src/main/resources/templates/User.html

Dossiers statiques:

- src/main/resources/static/css
- src/main/resources/static/images

Scripts utiles de diagnostic/test:

- diagnostic_403.sh
- test_serveur.sh
- test_serveur_complet.sh
- test-sms.sh

### 3) Utilisation (utilisateurs finaux)

Parcours type:

1. Ouvrir l application web.
2. Se connecter via l ecran de login.
3. Consulter l historique des messages/alertes.
4. Consulter les statistiques par site.
5. Utiliser les pages de gestion (si droits autorises).

[ESPACE RESERVE - CAPTURE ECRAN 1: PAGE LOGIN]

[ESPACE RESERVE - CAPTURE ECRAN 2: PAGE HISTORIQUE]

[ESPACE RESERVE - CAPTURE ECRAN 3: PAGE STATISTIQUES]

[ESPACE RESERVE - CAPTURE ECRAN 4: PAGE GESTION AGENT/USER]

---

## Concepts

### 1) Patterns utilises

- Architecture en couches:
  - Controller -> Service -> Repository -> Base.
- Pattern repository (Spring Data JPA).
- Separation Web (pages) et API (JSON), avec partage des services metier.

### 2) Philosophie du systeme

- Priorite au suivi operationnel terrain.
- Priorite a la tracabilite des alertes et de leurs statuts.
- Systeme hybride web + mobile/sms pour environnements de connectivite variable.

### 3) Decisions d architecture (principales)

- Centraliser la logique metier dans les services.
- Garder PostgreSQL comme source unique de verite des donnees.
- Utiliser la synchronisation API pour decoupler client mobile et interface web.

### 4) Approche Diataxis appliquee

- Tutoriels: section Guides d Utilisation (installation + parcours).
- How-to guides: exemples de configuration et d appel API.
- Reference: sections Architecture, API (table endpoints), fichiers critiques.
- Explication: section Concepts et section Limites/Contraintes.

Modularite retenue:

- sections autonomes et maintenables,
- chaque section peut etre mise a jour sans reecriture totale.

---

## Limites et Contraintes

### 1) Contexte d utilisation

- Systeme utilise par profils techniques et non techniques.
- Dependance forte a la qualite des donnees entrantes (messages mobiles/chiffres).
- Dependance a la disponibilite PostgreSQL et services reseau.

### 2) Limitations connues (etat actuel)

- Certaines routes sont trop permissives cote securite.
- Gestion des mots de passe encore non suffisamment durcie.
- Validation d entree encore partiellement manuelle selon endpoint.
- Retours d erreurs non totalement uniformises sur toutes les APIs.
- Documentation payload detaillee encore partiellement implicite dans le code.

### 3) Contraintes techniques

- Coherence des donnees dependante des regles SQL + logique Java combinees.
- Couplage numero de telephone -> site critique pour la bonne affectation.
- Conversion en docx: les diagrammes markdown avances ne sont pas toujours bien rendus.

### 4) Recommandations pour utilisateurs

- Respecter strictement les formats de message attendus cote mobile/API.
- Verifier la configuration avant lancement (DB, port, credentials, cles).
- Utiliser les scripts de diagnostic en cas d erreur 403 ou de non reponse API.

---

## Annexes de reference

Sources techniques principales:

- pom.xml
- src/main/resources/application.properties
- src/main/java/com/example/serveur/controller/ServeurController.java
- src/main/java/com/example/serveur/controller/SyncController.java
- src/main/java/com/example/serveur/controller/login/LoginController.java
- src/main/java/com/example/serveur/controller/Historique/HistoriqueController.java
- src/main/java/com/example/serveur/controller/Statistiques/StatistiqueController.java
- ../bdd/structure_lisible.sql
- ../bdd/data_lisible.sql
