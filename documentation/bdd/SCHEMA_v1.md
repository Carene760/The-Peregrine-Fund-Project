# Schéma de la Base de Données - Version 1.0

**Date de création** : 2025-01-15
**Version** : 1.0
**Statut** : Production

---

## 📊 Vue d'ensemble du schéma

La base de données `tpf` (The Peregrine Fund) est une application de gestion d'alertes environnementales pour les sites protégés à Madagascar. Elle suit une architecture **relationnelle normalisée** avec les principes suivants :

- **Séparation des domaines** : Données géographiques, utilisateurs, messages d'alerte, interventions
- **Audit temporel** : Historique des statuts et modifications
- **Sécurité** : Rôles d'accès et permissions par table
- **Intégrité référentielle** : Contraintes de clés étrangères strictes

---

## 🗺️ Diagramme des relations (ER)

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

---

## 📋 Description détaillée des tables principales

### 1. **site** (Zones protégées)

Représente les **zones géographiques protégées** à surveiller.

| Colonne | Type | Null | Clé | Description |
|---------|------|------|-----|-------------|
| id_site | SERIAL | NO | PK | Identifiant unique |
| nom_site | VARCHAR(100) | NO | | Nom de la zone (ex: "Menabe Nord") |
| localisation | VARCHAR(200) | YES | | Description géographique |
| latitude | DECIMAL(10,8) | YES | | Coordonnée GPS latitude |
| longitude | DECIMAL(11,8) | YES | | Coordonnée GPS longitude |
| zone | VARCHAR(50) | YES | | Région administrative (ex: "Sofia") |
| description | TEXT | YES | | Détails supplémentaires |
| date_creation | TIMESTAMP | NO | | Date de création du record |

**Exemple de données** :
```
1  | Menabe Central    | District de Menabe  | -19.75 | 45.50 | Menabe  | Zone protégée centrale
2  | Sofia Ouest      | Région Sofia        | -18.25 | 43.75 | Sofia   | Zone côtière ouest
```

---

### 2. **patrouilleur** (Agents terrain)

Représente les **agents de patrouille** affectés aux sites.

| Colonne | Type | Null | Clé | Description |
|---------|------|------|-----|-------------|
| id_patrouilleur | SERIAL | NO | PK | Identifiant unique |
| id_site | INT | NO | FK | Référence au site assigné |
| nom_patrouilleur | VARCHAR(100) | NO | | Nom complet de l'agent |
| prenom_patrouilleur | VARCHAR(100) | NO | | Prénom |
| email_patrouilleur | VARCHAR(100) | YES | | Email de contact |
| telephone | VARCHAR(20) | YES | | Numéro de téléphone |
| date_embauche | DATE | NO | | Date de démarrage |
| statut | VARCHAR(20) | NO | | Actif/Inactif |
| date_creation | TIMESTAMP | NO | | Timestamp création |

**Contraintes de clé étrangère** :
```sql
FOREIGN KEY (id_site) REFERENCES site(id_site) ON DELETE CASCADE
```

---

### 3. **userapp** (Comptes utilisateurs)

Représente les **utilisateurs** de l'application web et mobile.

| Colonne | Type | Null | Clé | Description |
|---------|------|------|-----|-------------|
| id_userapp | SERIAL | NO | PK | Identifiant unique |
| id_patrol | INT | NO | FK | Référence au patrouilleur |
| id_fonction | INT | YES | FK | Rôle/fonction de l'utilisateur |
| login | VARCHAR(100) | NO | U | Identifiant de connexion (unique) |
| password | VARCHAR(255) | NO | | Mot de passe hashé |
| email | VARCHAR(100) | YES | | Adresse email |
| actif | BOOLEAN | NO | | Compte actif/désactivé |
| date_creation | TIMESTAMP | NO | | Timestamp création |

**Contraintes** :
```sql
UNIQUE (login)
FOREIGN KEY (id_patrol) REFERENCES patrouilleur(id_patrouilleur)
FOREIGN KEY (id_fonction) REFERENCES fonction(id_fonction)
```

---

### 4. **message** (Rapports d'alerte)

Représente les **messages d'alerte** envoyés par les agents.

| Colonne | Type | Null | Clé | Description |
|---------|------|------|-----|-------------|
| id_message | SERIAL | NO | PK | Identifiant unique |
| id_userapp | INT | NO | FK | Utilisateur qui a créé l'alerte |
| id_site | INT | NO | FK | Site concerné |
| id_alerte | INT | YES | FK | Type d'alerte |
| contenu_message | TEXT | NO | | Texte du rapport |
| contenu_message_chiffre | BYTEA | YES | | Contenu chiffré (AES-128) |
| latitude | DECIMAL(10,8) | YES | | Coordonnée GPS |
| longitude | DECIMAL(11,8) | YES | | Coordonnée GPS |
| date_message | TIMESTAMP | NO | | Date/heure du rapport |
| statut | VARCHAR(20) | NO | | Statut (nouveau/en cours/résolu) |
| date_creation | TIMESTAMP | NO | | Timestamp création |

**Chiffrement** :
- `contenu_message_chiffre` utilise l'algorithme **AES-128** (EncryptionUtil.java)
- Clé stockée dans `application.properties` (parameter: `app.encryption.key`)

---

### 5. **alerte** (Niveaux d'escalade)

Représente les **types d'alerte** et niveaux de sévérité.

| Colonne | Type | Null | Clé | Description |
|---------|------|------|-----|-------------|
| id_alerte | SERIAL | NO | PK | Identifiant unique |
| id_typealert | INT | NO | FK | Type d'alerte |
| code_alerte | VARCHAR(20) | NO | U | Code unique (ex: "FEU_NIVEAU3") |
| seuil_escalade | INT | NO | | Seuil de gravité (1-5) |
| notification_active | BOOLEAN | NO | | Envoyer notification SMS/Email |
| description | TEXT | YES | | Description détaillée |
| date_creation | TIMESTAMP | NO | | Timestamp création |

**Référence** :
```sql
FOREIGN KEY (id_typealert) REFERENCES typealerte(id_typealert)
```

---

### 6. **typealerte** (Catalogue d'alertes)

Répertoire **centralisé** des types d'alertes possibles.

| Colonne | Type | Null | Clé | Description |
|---------|------|------|-----|-------------|
| id_typealert | SERIAL | NO | PK | Identifiant unique |
| nom_alerte | VARCHAR(100) | NO | | Nom du type (ex: "Feu de brousse") |
| code_type | VARCHAR(20) | NO | U | Code court |
| description | TEXT | YES | | Description |
| couleur_alerte | VARCHAR(7) | YES | | Code couleur HTML (#FF0000) |
| date_creation | TIMESTAMP | NO | | Timestamp création |

**Données initiales** :
```sql
('Feu de brousse', 'FEU', '...', '#FF0000')
('Braconnage', 'BRACONNAGE', '...', '#FF6600')
('Intrusion', 'INTRUSION', '...', '#FFFF00')
```

---

## 🔍 Autres tables

### Tables de référence/statut

| Table | Rôle | Colonnes principales |
|-------|------|----------------------|
| **fonction** | Rôles utilisateurs | id_fonction, nom_fonction, permissions |
| **intervention** | Types d'interventions | id_intervention, type, description |
| **status_message** | États d'un message | numero_status, nom_status, description |
| **status_agent** | États d'un agent | numero_agent_status, nom_status, description |

### Tables d'audit

| Table | Rôle | Colonnes principales |
|-------|------|----------------------|
| **historique_message_status** | Suivi des changements | id_message, ancien_statut, nouveau_statut, date, user_id |
| **historique_status_agent** | Actions des agents | id_agent, ancien_statut, nouveau_statut, date, action |

---

## 🔐 Intégrité référentielle

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

## 📈 Statistiques et taille estimée

| Aspect | Valeur |
|--------|--------|
| **Nombre total de tables** | 20+ |
| **Taille estimée** | 50-100 MB (données + index) |
| **Enregistrements estimés** | sites: 5-10, utilisateurs: 20-50, messages: 10 000+ |
| **Clés primaires** | SERIAL (auto-increment) |
| **Encodage** | UTF-8 |

---

## 🔄 Flux de données principal

```
1. Agent en terrain → Soumet message (contient GPS, contexte)
2. Message enregistré → Crée alerte automatiquement
3. Alerte → Triggers notifications (SMS, Email via SendGrid)
4. Administrateur → Consulte via interface web
5. Historique → Enregistre tous les changements de statut
6. Rapport → Statistiques agrégées par site/période
```

---

## 💾 Type d'indexation

**Index par défaut** (sur clés primaires et étrangères) :
```sql
CREATE INDEX idx_message_user ON message(id_userapp);
CREATE INDEX idx_message_site ON message(id_site);
CREATE INDEX idx_message_date ON message(date_message);
CREATE INDEX idx_patrouilleur_site ON patrouilleur(id_site);
CREATE INDEX idx_userapp_patrol ON userapp(id_patrol);
```

---

## 🛡️ Points de sécurité importants

1. **Chiffrement des messages** : Stockage dual (texte brut + chiffré AES-128)
2. **Authentification** : Hashage des mots de passe en base
3. **Audit** : Tables historique pour traçabilité complète
4. **Séparation des rôles** : Table `fonction` pour permissions graduées
5. **Validation en base** : NOT NULL sur colonnes critiques

---

## 📝 Conventions de nommage

| Élément | Convention | Exemple |
|---------|-----------|---------|
| Tables | Singulier, minuscules | `message`, `alerte`, `site` |
| Clés Primaires | `id_` + nom court | `id_message`, `id_site` |
| Clés Étrangères | `id_` + table référencée | `id_userapp`, `id_site` |
| Boolean | Colonne simple sans préfixe `is_` | `actif`, `notification_active` |
| Timestamps | `date_` + contexte | `date_creation`, `date_message` |

---

**Dernière mise à jour** : 2025-01-15
**Prochaine version** : Sera créée en `SCHEMA_v2.md` si modifications majeures
