# Architecture globale du module Site - Version 1

Date de creation: 2026-04-09
Version: 1.0

---

## 1) Vue globale

Le module Site est un serveur Spring Boot avec:

- interface web Thymeleaf,
- endpoints REST pour sync et gateway SMS/Internet,
- logique metier dans les services,
- persistance PostgreSQL via JPA repositories,
- securite applicative via Spring Security,
- integrations externes (SMS gateway, SendGrid).

---

## 2) Representation architecture (texte)

```text
[Android / Client web / Gateway SMS]
                |
                v
        +--------------------+
        |   Controllers      |
        |  (Web + REST API)  |
        +--------------------+
                |
                v
        +--------------------+
        |      Services      |
        | Regles metier      |
        +--------------------+
                |
                v
        +--------------------+
        |   Repositories     |
        | Spring Data JPA    |
        +--------------------+
                |
                v
        +--------------------+
        | PostgreSQL (tpf)   |
        +--------------------+

Transverse:
- SecurityConfig
- EncryptionUtil
- SmsLoggingService
- EmailService
```

[IMAGE PLACEHOLDER: Architecture globale Site v1]

---

## 3) Composants les plus importants

### Controleurs critiques

- `ServeurController`: point d entree principal API (`/api/*`) pour webhook, login, alerte, update status.
- `SyncController`: upload/download et synchronisation (`/sync/*`).
- `LoginController`: login web et session.

### Services critiques

- `SmsProcessingService`: dechiffrement, detection type de message (simple/login/alerte).
- `AlerteService`: parsing alerte, validation, persistance message, creation historique, declenchement niveau.
- `NiveauAlerteService`: calcul niveau Vert/Jaune/Orange/Rouge.
- `InfoRetourService`: message de retour structure (localisation + stats).
- `AllowedNumbersService`: controle des numeros autorises.
- `SmsResponseService`: envoi accuse/reponse vers gateway.
- `EmailService`: notifications email par zone d alerte.

### Configuration critique

- `SecurityConfig`: routes exposees et regles auth.
- `application.properties`: source de verite des parametres runtime.

---

## 4) Flux principal: message entrant -> alerte

```text
1) /api/webhook ou /api/message recoit message
2) verification numero autorise
3) dechiffrement / normalisation message
4) detection type (simple/login/alerte)
5) si alerte: processAlerte
6) creation Message + Historique
7) calcul niveau alerte
8) notification SMS/email + info retour
```

[IMAGE PLACEHOLDER: Flux message -> alerte -> notification]

---

## 5) Flux principal: mise a jour de statut

```text
1) /api/update-status recoit payload
2) verification numero autorise
3) parse format date/id_message/id_status
4) update historique statut message
5) reponse de confirmation
```

---

## 6) Routes majeures (extrait)

- `/api/webhook`
- `/api/login`
- `/api/message-alerte`
- `/api/update-status`
- `/api/message`
- `/sync/upload`
- `/sync/download/{idUser}`
- `/sync/historique/{idUser}`
- `/sync/interventions`
- `/sync/status`

---

## 7) Dependances techniques

Extrait stack (pom):

- Spring Boot 3.3.2
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-thymeleaf
- PostgreSQL driver
- SendGrid Java SDK

---

## 8) Points d architecture a surveiller

- `SecurityConfig` autorise de nombreuses routes: verifier l exposition en prod.
- secrets presents en configuration: externaliser selon environnement.
- robustesse parsing messages: garder les validations strictes.
- couplage metier API/gateway: formaliser des contrats de payload stables.

Derniere mise a jour: 2026-04-09
