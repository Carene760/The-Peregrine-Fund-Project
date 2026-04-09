# Architecture globale du module Site - Version 2

Date de creation: 2026-04-10
Version: 2.0

---

## 1) Vue globale

Le module Site v2 conserve la base Spring Boot de v1, mais introduit un contrat de messages plus stable.

Architecture globale:

- Controllers REST/Web,
- Services metier,
- Repositories JPA,
- PostgreSQL,
- Integrations externes (SMS gateway, SendGrid).

---

## 2) Changement d architecture cle (vs v1)

### Avant (v1)

- Detection du type de message basee sur le nombre de separateurs.
- Parsing positionnel (index fixes).

### Maintenant (v2)

- Detection du type par champ explicite `type`.
- Parsing cle=valeur (format flexible).
- Ajout de `date_envoi` dans le contrat metier.

---

## 3) Representation cible v2

```text
[Gateway SMS / Client mobile / client web]
                |
                v
        +--------------------+
        | ServeurController  |
        | + router type=...  |
        +--------------------+
                |
                v
        +-----------------------------+
        | SmsProcessingService        |
        | parser cle=valeur (v2)      |
        | validation champs oblig.    |
        +-----------------------------+
                |
                v
        +--------------------+
        | AlerteService      |
        | map -> Message     |
        | inclut date_envoi  |
        +--------------------+
                |
                v
        +--------------------+
        | Repositories JPA   |
        +--------------------+
                |
                v
        +--------------------+
        | PostgreSQL (tpf)   |
        +--------------------+

Transverse:
- SecurityConfig
- SmsResponseService
- Service webhook gateway (create/delete)
```

[IMAGE PLACEHOLDER: Architecture Site v2]

---

## 4) Contrats de messages v2

### ALERTE

```text
type=ALERTE/dateCommencement=.../dateSignalement=.../idIntervention=.../renfort=.../direction=.../surfaceApproximative=.../pointRepere=.../description=.../idUserApp=.../longitude=.../latitude=.../idStatus=.../date_envoi=...
```

### LOGIN

```text
type=LOGIN/login=.../password=...
```

### STATUS

```text
type=STATUS/dateChangement=.../idMessage=.../idStatus=...
```

---

## 5) Composants impactes par v2

- `SmsProcessingService`: parser V2 + validation.
- `ServeurController`: routage type explicite.
- `AlerteService`: lecture champs V2 et fallback date_envoi.
- `Message` (model): mappage `date_envoi`.
- service webhook gateway: creation/suppression de webhook.

---

## 6) Risques et mitigations

Risque principal:
- rupture des clients legacy.

Mitigation recommandee:
- mode dual temporaire (v1 legacy + v2),
- logs sur source parser,
- bascule progressive vers v2 only.

Derniere mise a jour: 2026-04-10
