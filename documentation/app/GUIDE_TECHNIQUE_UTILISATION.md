# Guide Technique et Guide d Utilisation - Application Android

## 1. Objectif
Ce document explique rapidement:
- le role de l application mobile
- le fonctionnement general
- les etapes d utilisation pour un utilisateur terrain
- les points de verification en cas de probleme

## 2. Perimetre fonctionnel
L application permet de:
- se connecter via SMS chiffre
- creer et envoyer un message d alerte (avec geolocalisation)
- synchroniser les donnees avec le serveur
- consulter l historique et modifier le statut d un message
- consulter l ecran de statistiques

## 3. Flux technique (version courte)
1. Login: envoi d un SMS chiffre avec identifiants.
2. Reception: decryptage de la reponse SMS puis ouverture du dashboard.
3. Creation alerte: saisie en 2 pages (infos generales + details).
4. Envoi: format metier chiffre puis envoi serveur/SMS.
5. Synchronisation: cascade statuts -> evenements -> interventions -> messages -> historique.

## 4. Configuration minimale
Fichier: `application/app/src/main/assets/config.properties`
- `secret.key`: cle AES (16 caracteres)
- `fixed.number`: numero de la passerelle SMS
- `server.url`: URL serveur principal
- `server.backup.url`: URL de secours

## 5. Guide d utilisation
### 5.1 Connexion
1. Ouvrir l application.
2. Saisir login et mot de passe.
3. Cliquer Sign In.
4. Attendre le SMS de confirmation.
5. Si la reponse contient un ID utilisateur, l application ouvre le dashboard.

### 5.2 Tableau de bord
1. Verifier la liste des messages historiques.
2. Utiliser le bouton `Sync` pour lancer une synchronisation manuelle.
3. Ouvrir un message pour voir ses details.
4. Mettre a jour le statut si necessaire (ex: En Cours, Maitrise).

### 5.3 Nouvelle alerte
1. Cliquer l icone Nouveau.
2. Page 1: renseigner commencement, statut, intervention, renfort, direction, surface.
3. Page 2: renseigner date/heure du signalement, point de repere, description.
4. Cliquer `Envoyer`.

### 5.4 Statistiques
1. Ouvrir l ecran Stat.
2. Consulter carte, pie chart, bar chart.
3. Verifier la lisibilite des labels (sites, zones, valeurs).

## 6. Captures d ecran (a inserer)
Placer les images dans: `documentation/app/captures/`

Captures recommandees:
- `capture-login.png` (ecran de connexion)
- `capture-dashboard.png` (dashboard + bouton sync)
- `capture-new-message-page1.png` (formulaire page 1)
- `capture-new-message-page2.png` (formulaire page 2)
- `capture-message-details.png` (detail message + statut)
- `capture-statistiques.png` (carte + graphiques)

Exemple d insertion markdown:

![Login](captures/capture-login.png)

## 7. Depannage rapide
- Login bloque sur "En attente": verifier numero fixe, cle AES, permissions SMS.
- Sync en erreur: verifier `server.url`, connectivite, endpoint backend.
- Champ cache par clavier: verifier `adjustResize` et le scroll actif du formulaire.
- Stats illisibles: ajuster labels axe X et legendes du graphique.

## 8. Reference code utile
- `LoginActivity`: authentification SMS
- `SmsReceiver`: reception + decryptage SMS
- `DashboardActivity`: historique, filtres, synchronisation
- `BaseActivity`: creation/envoi message
- `StatActivity`: affichage statistiques
- `SyncService`: pipeline de synchronisation

---
Document volontairement concis. Pour le detail technique complet, voir les fichiers dans `documentation/app/v1/`.
