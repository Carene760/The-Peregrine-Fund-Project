# Journal des modifications - CHANGELOG

**Format** : Sémantique de versioning (SCHEMA_v1, SCHEMA_v2, etc.)
**Destinataires** : Développeurs et administrateurs base de données

---

## 📅 Historique des versions

### 2025-01-15 | Version initiale (1.0)

**État** : ✅ **Production - Stable**

#### ✨ Créé
- `README.md` v1 - Index et guide d'accès pour la documentation BDD
- `SCHEMA_v1.md` - Documentation complète du schéma :
  - 20+ tables documentées
  - Diagramme ER complet
  - Description détaillée des 6 tables principales
  - Contraintes de clés étrangères
  - Type d'indexation
  - Conventions de nommage
- `EXECUTION_v1.md` - Guide d'installation et exécution :
  - Prérequis (PostgreSQL 16.4+)
  - Configuration du serveur
  - Procédure 4 phases (Créer DB → Schéma → Données → Sauvegarde)
  - Vérification de l'installation
  - Dépannage détaillé (5 cas courants)
  - Procédures de sauvegarde et restauration
- `CHANGELOG.md` - Ce fichier

#### 📊 Contenu de la base de données
- **Tables principales** : 
  - `site` (5 enregistrements)
  - `patrouilleur` (5 agents)
  - `userapp` (10 utilisateurs)
  - `message` (26 alertes)
  - `alerte` (8 types)
  - `typealerte` (catalogue de types)
  
- **Tables d'audit** :
  - `historique_message_status`
  - `historique_status_agent`

- **Tables de référence** :
  - `fonction` (rôles)
  - `intervention`
  - `status_message`
  - `status_agent`

#### ⚙️ Configuration initiale
- Encodage : UTF-8
- Localisation : en_US
- Chiffrement messages : AES-128 (EncryptionUtil.java)
- Serveur PostgreSQL : 16.4+
- Connexion : localhost:5432

#### 🔐 Sécurité appliquée
- Hachage des mots de passe en base
- Chiffrement des messages sensibles
- Contraintes d'intégrité référentielle
- Rôles d'accès par table `fonction`
- Historique audit complet

#### 🐛 Problèmes connus résolus
- Encodage UTF-16 de `data.sql` → Solution : utiliser `data_lisible.sql` (UTF-8)
- Séquences SERIAL non réinitialisées après import → Solution : `setval()` documentation

---

## 📋 Plan pour les prochaines versions

### Pour SCHEMA_v2 (Évolution)
Sera créé quand :
- Nouvelles tables ajoutées
- Restructuration majeure du schéma
- Changement de relations entre tables
- Modification des types de données

**Pré-requis** :
- Copier `SCHEMA_v1.md` → `SCHEMA_v2.md`
- Mettre à jour le diagramme ER
- Ajouter entrée dans CHANGELOG
- Conserver `SCHEMA_v1.md` pour traçabilité

### Pour EXECUTION_v2 (Procédures)
Sera créé quand :
- PostgreSQL upgraded (ex: 17.x)
- Procédure d'installation simplifiée ou changée
- Nouveaux outils d'automatisation
- Scripts de migration introduits

### Pour README (Mise à jour)
- Ajouter liens vers nouvelles versions
- Mettre à jour FAQ si procédures changent
- Mettre à jour date "Dernière mise à jour"

---

## 🔄 Procédure de mise à jour pour les contributeurs

### Quand mettre à jour la documentation ?

1. **Modification table existante** :
   - Ajouter colonne → Mettre à jour SCHEMA_v1.md section table
   - Changer type de données → Ajouter note en SCHEMA_v2.md (nouvelle version)
   - Changer contraintes → Documenter en SCHEMA_v2.md

2. **Ajout nouvelle table** :
   - Créer section dans SCHEMA_v1.md ou SCHEMA_v2.md
   - Mettre à jour diagramme ER
   - Ajouter ligne entrée ici dans CHANGELOG

3. **Changement procédure d'exécution** :
   - Mettre à jour EXECUTION_v1.md pour modifications mineures
   - Créer EXECUTION_v2.md pour changement majeur

4. **Correction/clarification** :
   - Mettre à jour le fichier existant (non versioning pour typos/clarifications)
   - Ajouter note ici dans CHANGELOG : "Correction typo section X"

---

## 📊 Matrice de versioning

| Document | Dernier | Créé quand | Action |
|----------|---------|-----------|--------|
| README.md | v1 | Initial | Mises à jour pour clarifications/FOG updates |
| SCHEMA_v1.md | 1.0 | Initial | Créer v2 si restructuration majeure |
| SCHEMA_v2.md | - | À venir | Quand nouvel état du schéma |
| EXECUTION_v1.md | 1.0 | Initial | Créer v2 si procédure change drastiquement |
| EXECUTION_v2.md | - | À venir | Quand processus d'installation change |
| CHANGELOG.md | N/A | Initial | Mis à jour après chaque modification |

---

## 🎯 Directives de communication des changements

Quand vous introduisez une **modification majeure** de schéma :

1. **Créer nouvelle version** du fichier concerné
2. **Ajouter entrée CHANGELOG** avec :
   - Date
   - Numéro version
   - Liste des changements
   - Impact sur les autres tables (si applicable)
   - Données de transition (si migration nécessaire)

**Exemple d'entrée** :
```markdown
### 2025-02-15 | SCHEMA_v2 - Ajout table d'audit supplémentaire

**État** : 🚧 En développement

#### 🆕 Nouvelles tables
- `historique_intervention` - Audit des interventions terrain

#### ✏️ Modifications
- Table `alerte` : +colonne `niveau_escalade_auto` (BOOLEAN)
- Table `message` : changement type `contenu_message_chiffre` (BYTEA → TEXT)

#### ⚠️ Impact
- Migrations requises pour données existantes
- Voir script `migrations/2025-02-15_add_audit_intervention.sql`

#### 🔄 Migration
```sql
ALTER TABLE message ALTER COLUMN contenu_message_chiffre TYPE TEXT;
ALTER TABLE alerte ADD COLUMN niveau_escalade_auto BOOLEAN DEFAULT false;
```
```

---

## 📈 Statistiques de la base de données

| Métrique | Valeur (v1) |
|----------|------------|
| Nombre de tables | 20+ |
| Clés primaires | 20+ (SERIAL) |
| Clés étrangères | 15+ |
| Index | ~10 |
| Procédures stockées | 0 (utilise JPA) |
| Triggers | 0 (utilise application logic) |
| Utilisateurs de base | 2 (postgres, tpf_user) |

---

## 🗺️ Dépendances documentaires

```
README.md (point d'entrée)
├── SCHEMA_v1.md (structure des données)
├── EXECUTION_v1.md (installation)
└── CHANGELOG.md (historique et directives)

[Futures versions]
└── SCHEMA_v2.md → SCHEMA_v3.md (versioning linéaire)
```

---

## ✅ Checklist pour une nouvelle version

Quand vous avez un changement majeure à documenter :

- [ ] Créer nouveau fichier `DOCUMENT_vN.md` (copie de v(N-1))
- [ ] Mettre à jour le contenu
- [ ] Mettre à jour les liens dans README.md
- [ ] Ajouter entrée détaillée dans CHANGELOG.md
- [ ] Archiver ancien fichier ou créer dossier `/archive/vieilles_versions/`
- [ ] Notifier l'équipe du changement (via email ou PR)
- [ ] Tester les procédures (notamment EXECUTION)

---

## 📞 Contact et questions

Pour questions sur l'une de ces documentations, consultez :

1. **Structure/schéma** → `SCHEMA_vX.md`
2. **Installation/exécution** → `EXECUTION_vX.md`
3. **Historique/versions** → Ce fichier (CHANGELOG.md)
4. **Guide général** → `README.md`

**Responsable** : Équipe développement + DBA
**Dernière vérification** : 2025-01-15

---

**Format** : Cette documentation suit le format **Markdown** pour compatibilité et versioning git.
**Sauvegarde** : Tous les fichiers `.md` sont versionnnés dans le dépôt git du projet.
