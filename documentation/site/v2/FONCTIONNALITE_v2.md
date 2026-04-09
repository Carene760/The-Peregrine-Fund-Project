# Regles metier et fonctionnalites du module Site - Version 2

Date de creation: 2026-04-10
Version: 2.0

---

## 1) Objectif v2

Reducer la dependance au format positionnel en adoptant un message a type explicite et champs cle=valeur.

---

## 2) Regles metier v2

### 2.1 Detection type de message

Nouvelle regle:
- le type est explicite: `type=ALERTE`, `type=LOGIN`, `type=STATUS`.

Effet:
- plus de dependance au nombre de separateurs.
- ajout de nouveaux champs sans casser la detection.

### 2.2 Parsing cle=valeur

Regle:
- split des tokens sur `/`,
- split de chaque token sur `=`,
- conversion dans une map,
- validation des champs obligatoires selon `type`.

### 2.3 Validation obligatoire par type

ALERTE:
- `dateSignalement`, `idIntervention`, `idUserApp`, `idStatus`.

LOGIN:
- `login`, `password`.

STATUS:
- `dateChangement`, `idMessage`, `idStatus`.

### 2.4 Gestion des champs inconnus

Regle:
- ignorer les champs non reconnus (forward compatibility).

### 2.5 Gestion de `date_envoi`

Regle:
- si `date_envoi` fourni dans message ALERTE -> persister cette valeur,
- sinon fallback vers `CURRENT_TIMESTAMP` cote BDD.

---

## 3) Exemples de messages v2

### ALERTE

```text
type=ALERTE/dateCommencement=2026-04-09T08:30:00/dateSignalement=2026-04-09T08:45:00/idIntervention=1/renfort=true/direction=Nord-Est/surfaceApproximative=2.5/pointRepere=Pres%20de%20la%20riviere/description=Fumee%20dense%20observee/idUserApp=5/longitude=47.5231/latitude=-18.9123/idStatus=1/date_envoi=2026-04-09T08:45:00
```

### LOGIN

```text
type=LOGIN/login=agent01/password=motdepasse123
```

### STATUS

```text
type=STATUS/dateChangement=2026-04-09T09:10:00/idMessage=120/idStatus=3
```

---

## 4) Webhook gateway auto (v2)

Objectif:
- ne plus dependre uniquement de Postman pour relier gateway et serveur.

Capacites v2 visees:
- creer un webhook depuis le systeme,
- supprimer un webhook depuis le systeme.

Exemple create:

```http
POST http://192.168.1.145:8080/webhooks
Content-Type: application/json

{
  "id": "test",
  "url": "https://a7e7c8d67145.ngrok-free.app/api/webhook",
  "event": "sms:received"
}
```

Exemple delete:

```http
DELETE http://192.168.88.5:8080/webhooks/<id_du_webhook>
```

---

## 5) Configuration v2 (application.properties)

Cles a normaliser:

```properties
gateway.base-url=http://192.168.1.145:8080
gateway.webhook.create-path=/webhooks
gateway.webhook.delete-path=/webhooks/{id}

gateway.webhook.id=test
gateway.webhook.event=sms:received
gateway.webhook.callback-url=https://a7e7c8d67145.ngrok-free.app/api/webhook

gateway.auth.username=sms
gateway.auth.password=39M6xJPK
```

---

## 6) Differences fonctionnelles vs v1

1. Detection message:
- v1: compteur de separateurs
- v2: type explicite

2. Evolution schema message:
- v1: pas de date_envoi metier
- v2: `date_envoi` integree

3. Lien gateway:
- v1: procedures manuelles dominantes
- v2: objectif d integration create/delete webhook dans le systeme

Derniere mise a jour: 2026-04-10
