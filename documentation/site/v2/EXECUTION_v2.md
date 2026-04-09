# Guide d execution du module Site - Version 2

Date de creation: 2026-04-10
Version: 2.0
Statut: migration v1 -> v2

---

## Table des matieres

1. Prerequis
2. Preparation BDD v2
3. Configuration application v2
4. Procedure de lancement
5. Validation fonctionnelle v2
6. Erreurs frequentes en migration
7. Checklist de bascule

---

## 1) Prerequis

- Java 17+
- Maven 3.9+
- PostgreSQL 16+
- Base `tpf`
- Scripts SQL v2 disponibles

---

## 2) Preparation BDD v2

Evolution obligatoire:

```sql
ALTER TABLE message ADD COLUMN date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

Verification:

```sql
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'message' AND column_name = 'date_envoi';
```

---

## 3) Configuration application v2

Dans `application.properties`, ajouter/normaliser:

```properties
# Parsing message v2
message.schema.mode=dual

# Gateway webhook
gateway.base-url=http://192.168.1.145:8080
gateway.webhook.create-path=/webhooks
gateway.webhook.delete-path=/webhooks/{id}
gateway.webhook.id=test
gateway.webhook.event=sms:received
gateway.webhook.callback-url=https://a7e7c8d67145.ngrok-free.app/api/webhook

# Auth gateway
gateway.auth.username=sms
gateway.auth.password=39M6xJPK
```

Notes:

- `message.schema.mode=dual` permet transition v1/v2.
- `gateway.webhook.callback-url` doit suivre l URL publique active (ngrok/domaine).

---

## 4) Procedure de lancement

Compilation:

```powershell
cd Site
mvn clean compile
```

Execution:

```powershell
mvn spring-boot:run
```

---

## 5) Validation fonctionnelle v2

### Test LOGIN v2

Envoyer:

```text
type=LOGIN/login=agent01/password=motdepasse123
```

Attendu:
- login valide,
- envoi accuse/reponse.

### Test ALERTE v2

Envoyer:

```text
type=ALERTE/dateCommencement=2026-04-09T08:30:00/dateSignalement=2026-04-09T08:45:00/idIntervention=1/renfort=true/direction=Nord-Est/surfaceApproximative=2.5/pointRepere=Pres%20de%20la%20riviere/description=Fumee%20dense%20observee/idUserApp=5/longitude=47.5231/latitude=-18.9123/idStatus=1/date_envoi=2026-04-09T08:45:00
```

Attendu:
- insertion message,
- `date_envoi` persistante,
- historique + niveau d alerte.

### Test STATUS v2

Envoyer:

```text
type=STATUS/dateChangement=2026-04-09T09:10:00/idMessage=120/idStatus=3
```

Attendu:
- mise a jour statut/historique OK.

### Test webhook auto

- create webhook -> 2xx
- delete webhook -> 2xx

---

## 6) Erreurs frequentes en migration

### Erreur: type absent

Cause:
- message v2 incomplet.

Correction:
- ajouter `type=...`.

### Erreur: champs obligatoires manquants

Cause:
- payload incomplet pour le type donne.

Correction:
- completer les champs requis.

### Erreur: callback webhook invalide

Cause:
- URL ngrok expiree/changée.

Correction:
- mettre a jour `gateway.webhook.callback-url` puis recreer webhook.

### Erreur: date_envoi non renseignee

Cause:
- parser ne mappe pas la cle ou valeur invalide.

Correction:
- verifier format ISO datetime.

---

## 7) Checklist de bascule

- [ ] colonne `date_envoi` presente en BDD
- [ ] parser v2 actif
- [ ] mode dual fonctionnel
- [ ] create/delete webhook operationnel
- [ ] tests LOGIN/ALERTE/STATUS v2 valides
- [ ] flux legacy encore fonctionnel pendant transition

Derniere mise a jour: 2026-04-10
