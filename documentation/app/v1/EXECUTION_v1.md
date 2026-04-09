# Guide d execution Android App - Version 1

Date de creation: 2026-04-09
Version: 1.0
Statut: dev/test

---

## Table des matieres

1. Prerequis
2. Fichiers obligatoires
3. Configuration par environnement
4. Procedure build et run
5. Verification fonctionnelle
6. Maintenance legere et erreurs frequentes
7. Checklist finale

---

## 1) Prerequis

### Outils obligatoires

- Android Studio recent
- JDK 11 (compileOptions Java 11)
- Android SDK compileSdk 36
- Appareil Android ou emulateur (API >= 21)

### Permissions et contexte mobile

L application utilise notamment:

- SEND_SMS
- RECEIVE_SMS
- READ_SMS
- INTERNET
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION

Ces permissions sont declarees dans AndroidManifest.

---

## 2) Fichiers obligatoires

Pour un fonctionnement correct:

- application/app/build.gradle
- application/app/src/main/AndroidManifest.xml
- application/app/src/main/assets/config.properties
- application/app/src/main/res/xml/network_security_config.xml
- application/app/src/main/java/com/example/theperegrinefund/LoginActivity.java
- application/app/src/main/java/com/example/theperegrinefund/BaseActivity.java
- application/app/src/main/java/com/example/theperegrinefund/DashboardActivity.java
- application/app/src/main/java/com/example/theperegrinefund/service/SyncService.java
- application/app/src/main/java/com/example/theperegrinefund/SmsSender.java
- application/app/src/main/java/com/example/theperegrinefund/SmsReceiver.java

---

## 3) Configuration par environnement

La config mobile est lue depuis assets/config.properties.

### Cles critiques

| Cle | Role | Obligatoire |
|---|---|---|
| secret.key | cle AES pour chiffrer/dechiffrer | Oui |
| fixed.number | numero cible SMS gateway | Oui |
| server.url | URL serveur principal | Oui |
| server.backup.url | URL serveur secours (sync) | Recommande |

### Exemple DEV

```properties
secret.key=VOTRE_CLE_AES_16_24_32
fixed.number=+261XXXXXXXXX
server.url=https://votre-serveur-dev
server.backup.url=https://votre-serveur-backup
```

### Notes securite

- ne pas publier de secrets reels dans git.
- prevoir un config par environnement (dev, test, prod).
- verifier la coherence avec le serveur Site (separateurs et format message).

---

## 4) Procedure build et run

### Build debug

Depuis le dossier application:

```powershell
.\gradlew clean
.\gradlew assembleDebug
```

### Installation sur appareil

```powershell
.\gradlew installDebug
```

### Lancement via Android Studio

- Ouvrir le projet application.
- Synchroniser Gradle.
- Choisir device/emulateur.
- Executer app module.

### Tests

```powershell
.\gradlew test
.\gradlew connectedAndroidTest
```

---

## 5) Verification fonctionnelle

1. Login SMS

- ouvrir LoginActivity
- saisir nom et mot de passe
- verifier envoi SMS chiffre
- verifier reception accuse et extraction ID utilisateur

2. Envoi alerte

- ouvrir BaseActivity
- remplir formulaire (fragments)
- recuperer GPS
- envoyer message SMS formate et chiffre

3. Synchronisation

- verifier telechargement status/interventions/messages/historique
- verifier persistence locale SQLite

4. Dashboard et historique

- verifier affichage historique local
- verifier changement de statut et envoi de l historique par SMS

5. Statistiques

- verifier parsing du message info retour
- verifier affichage carte (OSMDroid)
- verifier PieChart et BarChart

---

## 6) Maintenance legere et erreurs frequentes

### Erreur 1 - Permission SMS refusee

Symptome:
- login ou envoi message inoperant

Correction:
- accorder SEND_SMS / RECEIVE_SMS / READ_SMS
- relancer le flux

### Erreur 2 - Configuration introuvable

Symptome:
- erreur ConfigLoader sur config.properties

Correction:
- verifier presence de application/app/src/main/assets/config.properties
- verifier nom exact des cles

### Erreur 3 - Dechiffrement impossible

Symptome:
- exceptions CryptoUtils

Cause:
- secret.key differente entre mobile et serveur

Correction:
- aligner la cle AES sur les deux cotes

### Erreur 4 - Synchronisation en echec

Symptome:
- erreurs HTTP ou timeout dans SyncService

Checks:
- server.backup.url atteignable
- format endpoints conforme serveur
- connectivite internet device

### Erreur 5 - GPS non disponible

Symptome:
- coordonnees nulles ou 0

Correction:
- activer localisation precise
- verifier permissions location
- tester en exterieur / simulateur avec position injectee

### Erreur 6 - Graphiques vides

Symptome:
- pie/bar chart sans donnees

Cause:
- dernier SMS info retour absent ou format invalide

Correction:
- verifier reception SMS contenant 3 blocs separes par '~'

---

## 7) Checklist finale

- [ ] Build debug OK
- [ ] App installee et demarree
- [ ] Permissions critiques accordees
- [ ] config.properties coherent
- [ ] Login SMS fonctionnel
- [ ] Envoi message/alerte fonctionnel
- [ ] Synchronisation executee sans erreur bloquante
- [ ] Ecrans Dashboard et Stat operants

Derniere mise a jour: 2026-04-09
