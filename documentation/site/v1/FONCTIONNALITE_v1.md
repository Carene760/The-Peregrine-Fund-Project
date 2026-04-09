# Regles metier et fonctionnalites du module Site - Version 1

Date de creation: 2026-04-09
Version: 1.0

---

## 1) Objectif

Ce document precise les regles metier implementees dans les services, en particulier:

- detection des types de messages,
- traitement d alertes,
- calcul de niveau d alerte,
- synchronisation des donnees,
- construction des reponses retour.

---

## 2) Regles metier principales

### 2.1 Detection du type de message (`SmsProcessingService`)

Regle:

- Compter le nombre d occurrences du separateur (`message.separator`, par defaut `/`).
- Si separateurs >= 11 -> `ALERTE`.
- Si separateurs == 1 -> `LOGIN`.
- Sinon -> `MESSAGE_SIMPLE`.

Effet:

- Le routage metier ne depend pas uniquement de l endpoint, mais aussi du format du contenu.

### 2.2 Nettoyage et dechiffrement message (`SmsProcessingService`)

Regles:

- Nettoyer les retours ligne/espaces parasites.
- Si le payload n est pas Base64, il est considere deja en clair.
- Sinon, dechiffrement via `EncryptionUtil`.

Effet:

- Le systeme accepte des messages en clair ou chiffres selon la source.

### 2.3 Authentification login mobile (`SmsProcessingService`)

Regle:

- Un login est valide si le format est `login/separateur/motDePasse` (2 parties).
- Verification par `UserAppRepository.findByLoginAndMotDePasse(...)`.

Limite actuelle:

- comparaison directe sur mot de passe applicatif (a renforcer a terme).

### 2.4 Controle des numeros autorises (`AllowedNumbersService`)

Regles:

- Charger les numeros autorises depuis les patrouilleurs.
- Rafraichir automatiquement toutes les 5 minutes.
- Rejeter les messages provenant d un numero non autorise.

Effet:

- Filtre de securite metier en entree avant tout traitement lourd.

---

## 3) Regles metier d alerte (`AlerteService` + `NiveauAlerteService`)

### 3.1 Format attendu d une alerte

- Le message alerte est parse sur 12 segments.
- En cas d ecart, le traitement renvoie une erreur explicite.

### 3.2 Validation des champs obligatoires

Champs critiques verifies:

- dates,
- id intervention,
- direction,
- id user,
- id status.

### 3.3 Prevention des doublons

Regle actuelle:

- un doublon est detecte sur couple `longitude/latitude` existant.

### 3.4 Persistance transactionnelle

Une alerte valide entraine:

1. creation du `Message`,
2. creation `HistoriqueMessageStatus`,
3. determination niveau,
4. creation de `Alerte` liee.

### 3.5 Determination du niveau (Vert/Jaune/Orange/Rouge)

Decision basee sur:

- status du message,
- type d intervention,
- besoin renfort.

Exemple de logique:

- statut maitrise -> Vert,
- debut de feu + intervention partielle -> Jaune,
- cas critiques et intervention impossible -> Orange/Rouge selon renfort.

### 3.6 Diffusion des alertes

Apres niveau calcule:

- ciblage des fonctions concernees par zone d alerte,
- recuperation des utilisateurs concernes,
- envoi email via `EmailService`.

### 3.7 Generation info retour (`InfoRetourService`)

Le message retour combine 3 blocs:

1. localisation,
2. pourcentage alertes par niveau,
3. nombre de messages par site.

Le separateur de blocs est `info.separator` (par defaut `~`).

---

## 4) Regles de mise a jour de statut

### Via endpoint `/api/update-status` ou priorite dans `/api/message`

Regles:

- format attendu: `date_changement/id_message/id_status`.
- si format valide, priorite a l update statut avant autre interpretation.

Effet:

- permet traitement rapide d evolution de message terrain.

---

## 5) Regles de synchronisation (`SyncController`)

- upload: insertion seulement si `dateSignalement` non deja presente.
- download: extraction par utilisateur.
- endpoints annexes: recuperation historique/interventions/status.

---

## 6) Regles de reponse et observabilite

- chaque traitement important est journalise (`SmsLoggingService`).
- les reponses d accuse peuvent etre renvoyees par SMS (`SmsResponseService`).
- en cas d erreur, message d erreur explicite renvoye au client.

---

## 7) Mapping service -> responsabilite

| Service | Responsabilite metier principale |
|---|---|
| `SmsProcessingService` | Detection type, decrypt, login parsing |
| `AlerteService` | Validation alerte, persistance, orchestration |
| `NiveauAlerteService` | Calcul niveau + creation alerte |
| `InfoRetourService` | Construction payload de retour |
| `AllowedNumbersService` | Controle whitelist numerique |
| `SmsResponseService` | Retour SMS chiffre/clair |
| `SmsLoggingService` | Trace des messages entrants |
| `EmailService` | Notification email par zone |
| `HistoriqueMessageStatusService` | Mise a jour de statut et historique |
| `MessageService` | CRUD et stats message par site |

---

## 8) Limites fonctionnelles observees (v1)

- regles de detection basees sur format texte (sensible aux formats non conformes),
- dedoublonnage geospatial minimal (lon/lat uniquement),
- gestion des secrets dans fichier de configuration a durcir,
- securite web permissive sur plusieurs routes (a revisiter pour prod).

Derniere mise a jour: 2026-04-09
