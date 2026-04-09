# Limites et conseils - SMSSync Master

Date de creation: 2026-04-09
Version: 1.0

---

## 1) Limites de SMSSync Master

### 1.1 Obsolescence du projet

- Documentation historique SMSSync: contenu principalement 2015.
- Derniere release observee: v3.1.1 (2017).
- Risque: non alignement avec contraintes Android modernes (permissions, execution en arriere-plan, politiques Play Store).

### 1.2 Compatibilite Android

- **Aucune version maximale officiellement garantie n est documentee** dans les sources officielles consultees.
- En pratique, plus la version Android est recente, plus le risque d instabilite est eleve.
- Recommendation projet: valider en tests reels sur les versions cibles avant production.

### 1.3 Limitations techniques frequentes

- Dependance forte au reseau local en mode interne.
- Dependance a la stabilite du smartphone gateway (batterie, veille, connectivite).
- Maintenance manuelle plus importante qu une passerelle SMS managée.

---

## 2) Conseils d exploitation

### 2.1 Court terme (si SMSSync est conserve)

- Dedicacer un smartphone gateway (pas usage personnel).
- Desactiver optimisations batterie pour l app gateway.
- Utiliser IP locale fixe / reservation DHCP.
- Surveiller logs serveur et tests quotidiens de bout en bout.

### 2.2 Moyen terme

- Prevoir une alternative active et maintenue.
- Isoler la couche gateway pour minimiser l impact de migration.
- Garder un format de message stable (normalisation payload).

### 2.3 Production

- Eviter d exposer directement une gateway mobile sur internet sans controle.
- Ajouter supervision et alerting en cas de rupture du flux SMS.

---

## 3) Alternatives possibles

Exemples d alternatives (a evaluer selon cout, couverture et contraintes):

- Passerelles SMS cloud (Twilio, Vonage, Infobip, MessageBird, etc.)
- Solutions modem GSM auto-hebergees (Kannel, Gammu, Jasmin)
- Autres apps Android gateway actives avec API HTTP equivalente

---

## 4) Si on change de gateway: quels fichiers modifier ?

### 4.1 Cote serveur Site

Fichiers principaux a ajuster:

- `Site/src/main/resources/application.properties`
  - `gateway.auth.username`
  - `gateway.auth.password`
  - `gateway.internal-send-url`
  - eventuels nouveaux parametres (token, timeout, endpoint path)

- `Site/src/main/java/com/example/serveur/service/SmsResponseService.java`
  - schema d authentification (Basic/Bearer/API key)
  - format JSON attendu par le nouveau provider
  - endpoint d envoi sortant

- `Site/src/main/java/com/example/serveur/controller/ServeurController.java`
  - adaptation du webhook entrant si payload change

### 4.2 Cote application Android (si impact)

Fichiers potentiellement concernes:

- `application/app/src/main/assets/config.properties`
  - `fixed.number`
  - `server.url`
  - `server.backup.url`

- `application/app/src/main/java/com/example/theperegrinefund/SmsSender.java`
  - format du message si protocole evolue

- `application/app/src/main/java/com/example/theperegrinefund/SmsReceiver.java`
  - format/decodage des reponses

- `application/app/src/main/java/com/example/theperegrinefund/service/SyncService.java`
  - endpoints HTTP de synchronisation

### 4.3 Documentation a mettre a jour

- `documentation/site/v1/FONCTIONNALITE_v1.md`
- `documentation/site/v1/EXECUTION_v1.md`
- `documentation/app/v1/FONCTIONNALITE_v1.md`
- `documentation/app/v1/EXECUTION_v1.md`
- ce dossier `documentation/gateway/SMSSync Master/`

---

## 5) Recommandation finale

- Conserver SMSSync uniquement en phase transitoire ou pilote controle.
- Pour un usage durable/production, investir dans une solution maintenue avec SLA et support.

Derniere mise a jour: 2026-04-09
