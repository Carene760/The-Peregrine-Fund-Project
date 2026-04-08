# Schéma de la Base de Données - Version 1.0

**Date de création** : 2025-01-15
**Version** : 1.0
**Statut** : Production

---

## Vue d'ensemble du schéma

La base de données `tpf` (The Peregrine Fund) est une application de gestion d'alertes environnementales pour les sites protégés à Madagascar. Elle suit une architecture **relationnelle normalisée** avec les principes suivants :

- **Séparation des domaines** : Données géographiques, utilisateurs, messages d'alerte, interventions
- **Audit temporel** : Historique des statuts et modifications
- **Sécurité** : Rôles d'accès et permissions par table
- **Intégrité référentielle** : Contraintes de clés étrangères strictes

---

## Diagramme des relations (ER)

```
┌─────────────────┐
│      site       │ (Zones protégées)
├─────────────────┤
│ PK: id_site     │
│ Localisation    │
└────────┬────────┘
         │
         │ 1:N
         ↓
┌─────────────────────┐
│    patrouilleur     │ (Agents sur le terrain)
├─────────────────────┤
│ PK: id_patrouilleur │
│ FK: id_site         │
│ Informations agent  │
└────────┬────────────┘
         │
         │ 1:N
         ↓
┌──────────────────┐
│     userapp      │ (Comptes utilisateurs)
├──────────────────┤
│ PK: id_userapp   │
│ FK: id_patrol    │
│ Authentification │
└────────┬─────────┘
         │
         │ 1:N
         ↓
┌──────────────────────┐      FK: id_alerte    ┌──────────────────┐
│      message         │◄──────────────────────┤      alerte       │
├──────────────────────┤                        ├──────────────────┤
│ PK: id_message       │                        │ PK: id_alerte    │
│ FK: id_userapp       │                        │ FK: id_typealert │
│ FK: id_site          │                        │ Niveau escalade  │
│ Contenu du message   │                        └──────────────────┘
│ Coordonnées GPS      │                               ↑
│ Chiffrement AES      │                               │ FK
└──────────┬───────────┘                               │
           │                                    ┌──────────────────┐
           │ FK                                 │   typealerte     │
           └────────────────────────────────────┤                  │
                                                │ PK: id_typealert │
                                                │ Types d'alerte   │
                                                └──────────────────┘

┌───────────────────────────────────────────────────────────────────┐
│                    Tables d'audit (Historique)                   │
├───────────────────────────────────────────────────────────────────┤
│ historique_message_status    : Suivi des changements de statut   │
│ historique_status_agent      : Suivi des actions des agents      │
└───────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────┐
│                    Tables de référence                            │
├───────────────────────────────────────────────────────────────────┤
│ status_message     : États possibles d'un message                 │
│ status_agent       : États possibles d'un agent                   │
│ fonction           : Rôles/fonctions des utilisateurs             │
│ intervention       : Types d'interventions possibles              │
└───────────────────────────────────────────────────────────────────┘
```

PK : Primary Key

FK : Foreign Key

1:N : relation one-to-many

---

## Autres tables

### Tables de référence/statut

| Table                    | Rôle                 | Colonnes principales                         |
| ------------------------ | --------------------- | -------------------------------------------- |
| **fonction**       | Rôles utilisateurs   | id_fonction, nom_fonction, permissions       |
| **intervention**   | Types d'interventions | id_intervention, type, description           |
| **status_message** | États d'un message   | numero_status, nom_status, description       |
| **status_agent**   | États d'un agent     | numero_agent_status, nom_status, description |

### Tables d'audit

| Table                               | Rôle                 | Colonnes principales                                     |
| ----------------------------------- | --------------------- | -------------------------------------------------------- |
| **historique_message_status** | Suivi des changements | id_message, ancien_statut, nouveau_statut, date, user_id |
| **historique_status_agent**   | Actions des agents    | id_agent, ancien_statut, nouveau_statut, date, action    |

---

## Intégrité référentielle

### Cascade on Delete

Quand on supprime un **site** :

```
site (DELETE) → patrouilleur (CASCADE) → userapp (CASCADE)
                                      ↓
                                   message (CASCADE)
```

### Contraintes de clés étrangères critiques

```sql
-- Patrouilleur rattaché à site
ALTER TABLE patrouilleur ADD CONSTRAINT fk_site 
  FOREIGN KEY (id_site) REFERENCES site(id_site) ON DELETE CASCADE;

-- UserApp rattaché à patrouilleur
ALTER TABLE userapp ADD CONSTRAINT fk_patrol 
  FOREIGN KEY (id_patrol) REFERENCES patrouilleur(id_patrouilleur);

-- Message rattaché à userapp ET site
ALTER TABLE message ADD CONSTRAINT fk_userapp 
  FOREIGN KEY (id_userapp) REFERENCES userapp(id_userapp);
ALTER TABLE message ADD CONSTRAINT fk_site 
  FOREIGN KEY (id_site) REFERENCES site(id_site) ON DELETE CASCADE;

-- Alerte rattachée à typealerte
ALTER TABLE alerte ADD CONSTRAINT fk_typealert 
  FOREIGN KEY (id_typealert) REFERENCES typealerte(id_typealert);
```

---

## Flux de données principal

```
1. Agent en terrain → Soumet message (contient GPS, contexte)
2. Message enregistré → Crée alerte automatiquement
3. Alerte → Triggers notifications (SMS, Email via SendGrid)
4. Administrateur → Consulte via interface web
5. Historique → Enregistre tous les changements de statut
6. Rapport → Statistiques agrégées par site/période
```

---

## Type d'indexation

**Index par défaut** (sur clés primaires et étrangères) :

```sql
CREATE INDEX idx_message_user ON message(id_userapp);
CREATE INDEX idx_message_site ON message(id_site);
CREATE INDEX idx_message_date ON message(date_message);
CREATE INDEX idx_patrouilleur_site ON patrouilleur(id_site);
CREATE INDEX idx_userapp_patrol ON userapp(id_patrol);
```

---

## Points de sécurité importants

1. **Chiffrement des messages** : Stockage dual (texte brut + chiffré AES-128)
2. **Authentification** : Hashage des mots de passe en base
3. **Audit** : Tables historique pour traçabilité complète
4. **Séparation des rôles** : Table `fonction` pour permissions graduées
5. **Validation en base** : NOT NULL sur colonnes critiques

---

## Conventions de nommage

| Élément          | Convention                           | Exemple                             |
| ------------------ | ------------------------------------ | ----------------------------------- |
| Tables             | Singulier, minuscules                | `message`, `alerte`, `site`   |
| Clés Primaires    | `id_` + nom court                  | `id_message`, `id_site`         |
| Clés Étrangères | `id_` + table référencée        | `id_userapp`, `id_site`         |
| Boolean            | Colonne simple sans préfixe `is_` | `actif`, `notification_active`  |
| Timestamps         | `date_` + contexte                 | `date_creation`, `date_message` |
