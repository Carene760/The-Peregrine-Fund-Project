# Regles metier et fonctionnalites Android App - Version 1

Date de creation: 2026-04-09
Version: 1.0

---

## 1) Objectif

Documenter les comportements metier Android qui doivent rester stables et compris par l equipe.

---

## 2) Regles metier principales

### 2.1 Authentification mobile par SMS

Composants:

- LoginActivity
- SmsSender
- SmsReceiver
- CredentialUtil / CryptoUtils

Regles:

- le couple nom/mot de passe est combine puis chiffre.
- le message est envoye au numero fixe configure.
- la reponse SMS dechiffree doit contenir un ID utilisateur.
- si ID detecte, l utilisateur est considere authentifie.

### 2.2 Format metier d un message alerte

Composant principal: SmsSender.formatMessage

Regle:

Le payload contient 12 segments separes par '/':

1. date commencement
2. date signalement
3. id intervention
4. renfort
5. direction
6. surface
7. point repere
8. description
9. id user app
10. longitude
11. latitude
12. id status

Impact:

- toute modification de ce format doit etre synchronisee avec le serveur.

### 2.3 Envoi message avec fallback serveur/SMS

Composant: ServerSender

Regles:

- tentative envoi via API serveur (Retrofit).
- si echec reseau serveur, fallback par envoi SMS.
- en cas d echec SMS egalement, retour erreur utilisateur.

### 2.4 Synchronisation descendante

Composant: SyncService

Regles:

- telecharger status -> interventions -> messages -> historique.
- parser JSON via Gson.
- inserer en SQLite via DAOs.
- rattacher les messages a l utilisateur courant.

### 2.5 Gestion des statuts depuis Dashboard

Composants:

- DashboardActivity
- HistoriqueMessageStatusDao
- SmsSender.sendHistory

Regles:

- action UI convertie en id status.
- creation d un enregistrement historique local.
- envoi de l historique au serveur via SMS chiffre.

### 2.6 Statistiques et cartographie

Composant: StatActivity

Regles:

- lire le dernier SMS du numero fixe.
- decrypter le corps.
- accepter seulement le format a 3 blocs: localisation~pourcentages~sites.
- alimenter:
  - carte centre + marqueurs,
  - pie chart des niveaux,
  - bar chart des messages par site.

---

## 3) Regles techniques liees a la securite

- chiffrement AES base64 utilise pour les echanges sensibles.
- la meme cle doit etre partagee entre app et serveur.
- les permissions Android SMS/Location sont bloquantes pour les flux principaux.

---

## 4) Mapping composant -> responsabilite

| Composant | Responsabilite |
|---|---|
| LoginActivity | Auth par SMS et session utilisateur mobile |
| BaseActivity | Saisie alerte, geolocalisation, envoi message |
| DashboardActivity | Historique, actions statut, sync UI |
| StatActivity | Carte + graphes a partir du retour serveur |
| SmsSender | Chiffrement et envoi SMS metier |
| SmsReceiver | Reception et dechiffrement SMS entrants |
| ServerSender | Priorite API puis fallback SMS |
| SyncService | Telechargement et persistence locale |
| MyDatabaseHelper | Schema SQLite local |
| DAOs | CRUD local messages/statuts/historique/interventions |

---

## 5) Limites fonctionnelles observees (v1)

- endpoint ApiService actuel `/api/send` doit rester aligne avec le serveur reel.
- certaines classes UI/flux sont encore en transition (ex: ecran SendAlerte minimal).
- SendMessage.java est present mais vide.
- configuration sensible en assets a proteger selon environnement de build.

Derniere mise a jour: 2026-04-09
