# Architecture globale Android App - Version 1

Date de creation: 2026-04-09
Version: 1.0

---

## 1) Vue d ensemble

L application Android combine:

- interface utilisateur multi ecrans,
- envoi et reception SMS chiffrés,
- stockage local SQLite,
- synchronisation serveur HTTP,
- visualisation cartes et statistiques.

---

## 2) Representation architecture

```text
[Utilisateur Mobile]
       |
       v
+-----------------------+
| Activities / Fragments|
| Login, Base, Dashboard|
| Stat, DbDebug         |
+-----------------------+
       |
       v
+-----------------------+
| Services metier       |
| SmsSender/SmsReceiver |
| SyncService           |
| ServerSender          |
+-----------------------+
       |
       +------------------------+
       |                        |
       v                        v
+-------------------+   +-------------------+
| SQLite local      |   | Serveur Site      |
| DAOs + helper DB  |   | API REST + SMS GW |
+-------------------+   +-------------------+

Transverse:
- CryptoUtils
- ConfigLoader
```

[IMAGE PLACEHOLDER: Architecture globale Android v1]

---

## 3) Composants critiques

### UI

- LoginActivity: authentification via SMS chiffre + reception accuse.
- BaseActivity: formulaire message (2 fragments), geolocalisation, envoi SMS.
- DashboardActivity: historique local, sync descendante, changements de statut.
- StatActivity: carte OSMDroid + PieChart + BarChart via message de retour.
- DbDebugActivity: inspection brute SQLite.

### Services

- SmsSender: formatage message metier et envoi SMS chiffre.
- SmsReceiver: reception SMS, dechiffrement, broadcast interne.
- SyncService: download status/interventions/messages/historique.
- ServerSender: tentative envoi serveur puis fallback SMS.

### Donnees locales

- MyDatabaseHelper: schema SQLite local (message, status, historique, intervention, users).
- DAOs: encapsulation des operations DB.

### Securite / config

- ConfigLoader: lecture des parametres dans assets/config.properties.
- CryptoUtils: chiffrement/dechiffrement AES Base64.
- network_security_config.xml: cleartext autorise pour domaine local defini.

---

## 4) Flux principal login

```text
1) User saisit nom/mot de passe
2) LoginActivity chiffre la charge utile
3) SmsSender envoie au numero fixe
4) SmsReceiver intercepte la reponse
5) Dechiffrement puis extraction ID user
6) Navigation vers Dashboard
```

[IMAGE PLACEHOLDER: Flux login SMS]

---

## 5) Flux principal envoi alerte

```text
1) User remplit formulaire (Fragment1 + Fragment2)
2) BaseActivity recupere GPS
3) SmsSender formate les 12 segments metier
4) Chiffrement AES puis envoi SMS
5) Serveur traite et renvoie info retour/statut
6) App met a jour historique et vues
```

[IMAGE PLACEHOLDER: Flux alerte mobile]

---

## 6) Flux principal synchronisation

```text
1) SyncService appelle endpoints serveur
2) telecharge status, interventions, messages, historique
3) mappe JSON vers modeles Android
4) persiste dans SQLite via DAOs
5) UI relit le stockage local
```

---

## 7) Dependances techniques majeures

- AndroidX core/appcompat/material
- Retrofit + Gson
- OkHttp
- OSMDroid
- MPAndroidChart
- Play Services Location

---

## 8) Points de vigilance architecture

- forte dependance au format des messages SMS.
- secrets en fichier assets a proteger selon environnement.
- robustesse reseau a renforcer sur fallback et retries.
- coherence schemas SQLite/serveur a surveiller.

Derniere mise a jour: 2026-04-09
