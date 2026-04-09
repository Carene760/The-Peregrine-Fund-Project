# Configuration et integration - SMSSync Master

Date de creation: 2026-04-09
Version: 1.0

---

## 1) Objectif

Decrire:

- la configuration SMSSync cote Android Gateway,
- la configuration serveur Spring Boot,
- l integration locale recommandee,
- les points de verification en exploitation.

---

## 2) Prerequis

- Un smartphone Android avec SIM active.
- Application SMSSync installee (v3.1.1 disponible dans /The-Perigrine-Fund-Project/gateway).
- Serveur Site demarre et joignable.
- Smartphone gateway et serveur sur le meme reseau local (mode recommande).

---

## 3) Parametres a configurer dans `application.properties`

Exemple minimal cote serveur:

```properties
# Auth Basic utilisee par le serveur pour appeler le endpoint interne de la gateway
gateway.auth.username=user_name_dans_application
gateway.auth.password=password

# URL interne de la gateway pour envoyer un SMS sortant depuis le serveur
gateway.internal-send-url=http://<adresse_ip_gateway>:<port_gateway>/message
```

### Parametres additionnels utiles (dans votre projet)

```properties
# Optionnel: whitelist numerique (si active dans votre code)
# gateway.allowed-numbers=+2613XXXXXXXX,+2613YYYYYYYY

# Logging utile en phase debug
logging.level.com.example.serveur.controller=TRACE
```

Notes:

- Dans votre code serveur, ces cles sont lues par `SmsResponseService`.
- L appel est fait en `POST` JSON avec Auth Basic.

---

## 4) Integration locale recommandee

### 4.1 Schema reseau

```text
[Android Gateway SMSSync] <--LAN/Wi-Fi--> [Serveur Spring Boot Site]
```

### 4.2 Contraintes

- Le mobile gateway et le serveur doivent etre sur le meme sous-reseau.
- L IP locale de la gateway doit etre stable (reservee DHCP si possible).
- Le firewall doit autoriser le port de la gateway et le port serveur.

### 4.3 Valeur type

```properties
gateway.internal-send-url=http://192.168.X.Y:8080/message
```

Adaptez `192.168.X.Y` a l IP du smartphone gateway.

---

## 5) Integration fonctionnelle avec votre serveur

### 5.1 Flux entrant (SMS -> serveur)

SMSSync envoie en HTTP POST des champs comme:

- `from`
- `message`
- `message_id`
- `secret`
- `device_id`
- `sent_timestamp`

Votre serveur doit retourner un JSON de succes:

```json
{
  "payload": {
    "success": true,
    "error": null
  }
}
```

### 5.2 Flux sortant (serveur -> SMS)

Votre serveur envoie une requete HTTP vers `gateway.internal-send-url` avec:

- Header `Authorization: Basic ...`
- Body JSON:

```json
{
  "phoneNumbers": ["+2613XXXXXXXX"],
  "message": "<message_chiffre_ou_clair>",
  "withDeliveryReport": true
}
```

Ce flux est implemente par `SmsResponseService`.

---

## 6) Swagger UI

- Pas de Swagger UI officiel public identifie pour SMSSync.
- Reference API officielle: page developers (payloads HTTP + JSON).

---

## 7) Verification rapide

1. Lancer le serveur Spring.
2. Configurer SMSSync avec Sync URL correcte.
3. Envoyer un SMS test vers le mobile gateway.
4. Verifier reception webhook cote serveur.
5. Verifier reponse JSON `success=true`.
6. Verifier un envoi sortant via `SmsResponseService`.

---

## 8) Checklist d exploitation

- [ ] credentials `gateway.auth.*` alignes entre serveur et gateway
- [ ] URL interne gateway correcte
- [ ] smartphone gateway sur le meme reseau local que serveur
- [ ] secret/protection webhook active
- [ ] logs de debug disponibles en phase test

Derniere mise a jour: 2026-04-09
