# The Peregrine Fund Project

Système d'alerte incendie pour rangers sur le terrain : une application
Android (envoi d'alertes/statuts, GPS, fonctionne même sans connexion via
SMS), un serveur central (Spring Boot + PostgreSQL) qui traite les alertes,
notifie les personnes concernées et centralise l'historique, et une
passerelle SMS (téléphone Android + SMSSync) qui relaie les messages quand
aucune connexion internet directe n'est disponible.

## Structure du dépôt

- **`Site/`** — Backend Spring Boot (API, traitement des alertes, tableau
  de bord web d'administration/historique).
- **`application/`** — Application Android pour les rangers sur le terrain.
- **`bdd/`** — Scripts SQL (structure de la base PostgreSQL).
- **`documentation/`** — Documents de conception/exécution antérieurs.
- **`loadtest/`** — Script de test de charge (voir plus bas).
- **`DEPLOYMENT.md`** — Marche à suivre pour un déploiement réel (non
  encore exécuté).
- **`SECURITY_REMEDIATION.md`** — État des secrets/clés et plan de purge
  de l'historique git (non encore exécuté).

## Nouveautés de cette version

### Fiabilité et performance serveur

- Connexion directe internet (HTTP) entre l'application et le serveur,
  avec repli automatique sur SMS uniquement si aucune connexion n'est
  disponible — évite de dépendre systématiquement de la passerelle SMS.
- Chiffrement AES/GCM (remplace l'ancien AES/ECB, non sécurisé).
- Correctifs de robustesse trouvés via test de charge : l'envoi de
  notifications (email, SMS aux fonctions concernées) était synchrone et
  sans timeout — un service externe injoignable bloquait indéfiniment la
  réponse au ranger. Rendu asynchrone + timeouts configurés.
- Pool de connexions à la base de données dimensionné (la valeur par
  défaut de Spring devenait un goulot d'étranglement sous charge
  concurrente).
- Testé à 25 utilisateurs simultanés (alertes + connexions + synchronisation
  mélangées) : 100% de réussite, zéro échec.

### Nouvelles fonctionnalités

- **Notification de changement de statut** : les personnes concernées sont
  désormais prévenues (SMS + email) quand le statut d'une alerte évolue
  (auparavant, seul l'expéditeur recevait un accusé optionnel).
- **Messages plus professionnels et explicites** : les SMS/emails d'alerte
  contiennent maintenant les vraies informations de l'incendie (position,
  intervention, description...) au lieu d'un texte générique par zone.
- **Multi-langue (français / anglais / malgache)** : sélecteur de langue
  dans l'application (icône 🌐) et sur le site (13 pages). La page de
  connexion du site est entièrement traduite comme exemple de référence ;
  les autres pages ont le sélecteur mais leur contenu reste en français
  pour l'instant (voir Limites).
- **Export JSON** de l'historique, en plus de CSV/XLSX.
- **Envoi d'email via SMTP Gmail** au lieu de SendGrid (compte tiers en
  souci) — configuration par variables d'environnement au déploiement.

### Interface et accessibilité

- Écran de connexion : logo qui n'était plus affiché en entier (rogné par
  un conteneur trop petit) corrigé ; champs et bouton adaptatifs à la
  taille de l'écran au lieu d'une largeur fixe.
- Icône de l'application corrigée (le nom de l'organisation était coupé).
- Page historique (site) : colonnes secondaires masquées automatiquement
  sur petit écran pour rester lisible ; filtre Année généré dynamiquement
  depuis les données réelles au lieu d'une liste figée.

### Corrections bloquant la publication

- Une image utilisée sur l'écran de connexion provenait d'une banque
  d'images sans licence (filigrane visible) — remplacée par une icône
  dessinée spécifiquement pour ce projet.
- Le logo de l'application était en réalité un fichier JPEG renommé en
  `.png`, ce qui faisait échouer la compilation en mode release (invisible
  en mode debug, donc jamais détecté avant).
- Un bug de mise en page (référence à un composant inexistant sur l'écran
  concerné) faisait également échouer la compilation release.
- **Résultat : le build release de l'application fonctionne à nouveau**
  (`app-release-unsigned.apk`) — restait bloqué avant cette version.

## Avantages de cette version

- Fonctionne en conditions de terrain à connectivité intermittente
  (internet direct quand disponible, SMS sinon, sans action de
  l'utilisateur).
- Fiabilité validée par un test de charge réaliste, pas seulement testée à
  la main.
- Notifications automatiques : les bonnes personnes sont informées sans
  intervention manuelle, à la création d'une alerte comme à son évolution.
- Pensée pour des utilisateurs peu familiers avec la technologie (langue
  maternelle disponible, messages clairs, interface simplifiée).
- Build de publication (release) à nouveau fonctionnel.

## Limites connues

- **Traduction incomplète** : beaucoup de messages ponctuels (Toasts,
  logs, confirmations) restent codés en dur en français dans le code, et
  12 des 13 pages du site n'ont pas encore leur contenu traduit (seul le
  sélecteur est présent).
- **APK de release non signé** : ne peut pas encore être installé/distribué
  tel quel (voir `DEPLOYMENT.md` §5 pour la génération du keystore).
- **Aucun déploiement réel** : le serveur ne tourne que localement pour
  l'instant ; un vrai test terrain (hors du réseau local) nécessite de
  suivre `DEPLOYMENT.md`.
- **Secrets encore en clair** dans les fichiers de configuration locaux
  (acceptable en développement, à corriger avant tout déploiement partagé
  — voir `SECURITY_REMEDIATION.md`).
- **Passerelle SMS physique** (téléphone + SMSSync) reste un point de
  fragilité intrinsèque (dépend d'un appareil unique allumé, chargé,
  connecté) — désormais bornée par des timeouts, mais pas éliminée.
- Aucune suite de tests automatisés (les vérifications de cette version
  ont été faites manuellement/par script de test de charge ponctuel).

## Vision future

- **Détection du feu le plus proche** : rattachement automatique d'une
  nouvelle alerte à un incident déjà actif à proximité (distance GPS),
  pour informer immédiatement le ranger du niveau de gravité déjà en
  cours. Nécessite de définir un seuil de distance et la notion
  d'« événement actif ».
- **Passerelle SMS moderne** (ex. Africa's Talking) pour remplacer le
  téléphone-passerelle physique par une API cloud fiable, adaptée au
  contexte malgache.
- **Traduction complète** : toutes les chaînes codées en dur, et les 12
  pages du site restantes.
- **Plateforme d'événements générique** : au-delà des feux de brousse,
  vision à plus long terme d'un système configurable pour d'autres types
  d'événements/alertes.
- **Déploiement réel** (base de données + hébergement managés) et
  signature de l'APK pour une distribution officielle.

## Test de charge

Un script de test de charge autonome (`loadtest/load-test.mjs`, Node.js,
sans dépendance externe) simule des rangers envoyant des alertes/se
connectant/synchronisant en parallèle, en chiffrant lui-même les messages
comme le ferait l'application. Voir les commentaires en tête du fichier
pour l'usage.
