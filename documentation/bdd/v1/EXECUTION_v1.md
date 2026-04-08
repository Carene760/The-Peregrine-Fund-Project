# Guide d'exécution - Installation et préparation de la base de données

**Date de création** : 2025-01-15
**Version** : 1.0
**Statut** : Production

---

## Table des matières

1. [Prérequis](#prérequis)
2. [Configuration du serveur PostgreSQL](#configuration-du-serveur-postgresql)
3. [Procédure d&#39;exécution par étapes](#procédure-dexécution-par-étapes)
4. [Vérification de l&#39;installation](#vérification-de-linstallation)
5. [Dépannage](#dépannage)
6. [Sauvegardes et restauration](#sauvegardes-et-restauration)

---

## Prérequis

### Obligatoire

- **PostgreSQL 16.4+** installé et démarré
- **Outil psql** (fourni avec PostgreSQL)
- **Accès administrateur** à PostgreSQL (utilisateur `postgres`)
- **Fichiers SQL** disponibles dans le dossier `/bdd/` du projet
- **Encodage UTF-8** configuré sur la base (par défaut sous PostgreSQL)

### Vérification de l'installation

Ouvrez un terminal (PowerShell/CMD/Bash) et exécutez :

```powershell
# Vérifier que PostgreSQL est installé
psql --version

# Résultat attendu : psql (PostgreSQL) 16.4 (ou plus récent)
```

Si vous obtenez "commande non reconnue" :

> PostgreSQL n'est pas ajouté au PATH. Ajoutez le chemin : `C:\Program Files\PostgreSQL\16\bin` au PATH Windows.

---

## Configuration du serveur PostgreSQL

### 1. Vérifier le service PostgreSQL

**Sous Windows** :

```powershell
# Vérifier que le service est actif
Get-Service | Where-Object {$_.Name -like "*postgres*"}

# Si arrêté, le démarrer
Start-Service -Name "postgresql-x64-16"  # Adaptez la version
```

**Sous Linux/Mac** :

```bash
sudo systemctl status postgresql
sudo systemctl start postgresql  # Si nécessaire
```

### 2. Créer un utilisateur PostgreSQL pour l'application

**OPTIONNEL** - À faire une seule fois

```bash
# Connectez-vous en administrateur
psql -U postgres

# Dans psql, créez un utilisateur dédié
CREATE USER tpf_user WITH PASSWORD 'VotreMotDePasse123';

# Donnez les droits de création de bases
ALTER USER tpf_user CREATEDB;

# Quittez psql
\q
```

---

## Procédure d'exécution par étapes

### Phase 1 : Créer la base de données

```bash
# Connectez-vous en administrateur PostgreSQL
psql -U postgres

# Créez la base de données
CREATE DATABASE tpf WITH
  ENCODING 'UTF8'
  LC_COLLATE = 'en_US.UTF-8'
  LC_CTYPE = 'en_US.UTF-8'
  OWNER postgres;

# Vérifiez la création
\l

# Connectez-vous à la nouvelle base
\c tpf
```

**Résultat attendu** :

```
CREATE DATABASE
You are now connected to database "tpf" as user "postgres".
```

---

### Phase 2 : Exécuter le schéma (structure)

Le fichier `structure.sql` crée la structure des tables, index et contraintes.

#### Option A : Avec fichier original (`structure.sql`)

```bash
# Depuis le répertoire contenant structure.sql
psql -U postgres -d tpf -f structure.sql

# Résultat : "CREATE TABLE" pour chaque table
```

#### Option B : Avec fichier lisible (`structure_lisible.sql`)

Recommandé pour meilleure lisibilité lors de dépannage :

```bash
# Depuis le répertoire /bdd/
psql -U postgres -d tpf -f structure_lisible.sql

# Résultat identique à l'option A
```

**Vérification** :

```bash
psql -U postgres -d tpf -c "\dt"

# Résultat attendu : Liste de toutes les tables créées
#              List of relations
# Schema |          Name           | Type  | Owner
#--------+-------------------------+-------+----------
# public | alerte                  | table | postgres
# public | fonction                | table | postgres
# public | historique_message_...  | table | postgres
# ...
```

---

### Phase 3 : Charger les données test (optionnel)

Le fichier `data.sql` insère les données de base (sites, utilisateurs, alertes types, etc.).

#### Option A : Avec fichier original (`data.sql`)

**ATTENTION** : Ce fichier peut avoir des problèmes d'encodage UTF-16

```bash
# Méthode 1 : Directe (peut échouer sur Windows)
psql -U postgres -d tpf -f data.sql

# Méthode 2 : Avec conversión d'encodage (PowerShell Windows)
Get-Content -Path "data.sql" -Encoding Unicode | psql -U postgres -d tpf
```

#### Option B : Avec fichier lisible (`data_lisible.sql`)

**RECOMMANDÉ** - Format UTF-8 standard

```bash
# Depuis le répertoire /bdd/
psql -U postgres -d tpf -f data_lisible.sql

# Résultat : "INSERT 0 X" pour chaque INSERT
```

**Vérification** :

```bash
psql -U postgres -d tpf -c "SELECT COUNT(*) as nombre_sites FROM site;"

# Résultat attendu : 
#  nombre_sites
# ──────────────
#            5
# (1 row)
```

---

## Vérification de l'installation

### 1. Vérifier la connexion

```bash
psql -U postgres -d tpf -c "SELECT NOW();"

# Résultat :
#              now          
# ──────────────────────────────
#  2025-01-15 14:30:45.123456+00
```

### 2. Vérifier les tables

```bash
psql -U postgres -d tpf << EOF
\dt
\di
\dF
EOF

# \dt = liste des tables
# \di = liste des index
# \dF = liste des fonctions
```

### 3. Vérifier les données

```bash
# Compter les records
psql -U postgres -d tpf << EOF
SELECT 'site' as table_name, COUNT(*) as count FROM site
UNION ALL
SELECT 'patrouilleur', COUNT(*) FROM patrouilleur
UNION ALL
SELECT 'userapp', COUNT(*) FROM userapp
UNION ALL
SELECT 'message', COUNT(*) FROM message
UNION ALL
SELECT 'alerte', COUNT(*) FROM alerte;
EOF

# Résultat attendu :
#    table_name    | count
# ─────────────────┼───────
#  site            |    5
#  patrouilleur    |    5
#  userapp         |   10
#  message         |   26
#  alerte          |    8
```

### 4. Vérifier les contraintes de clés étrangères

```bash
psql -U postgres -d tpf -c "
  SELECT constraint_name, table_name, column_name 
  FROM information_schema.key_column_usage 
  WHERE table_schema = 'public' 
  ORDER BY table_name;
"

# Résultat : Liste de toutes les clés et contraintes
```

---

## Dépannage

### Erreur : "psql: commande non trouvée"

**Cause** : PostgreSQL n'est pas dans le PATH

**Solution Windows** :

```powershell
# Ajouter au PATH temporairement
$env:Path += ";C:\Program Files\PostgreSQL\16\bin"

# Vérifiez
psql --version
```

### Erreur : "FATAL: database "tpf" does not exist"

**Cause** : La base n'a pas été créée

**Solution** :

```bash
# Créez-la
psql -U postgres -c "CREATE DATABASE tpf;"

# Ou réexécutez la Phase 1 ci-dessus
```

### Erreur : "permission denied for schema public"

**Cause** : Utilisateur sans droits d'accès

**Solution** :

```bash
psql -U postgres -d tpf -c "
  GRANT USAGE ON SCHEMA public TO tpf_user;
  GRANT CREATE ON SCHEMA public TO tpf_user;
  GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO tpf_user;
"
```

### Erreur : "Encoding error", "invalid byte sequence"

**Cause** : Fichier SQL en encodage incorrect (UTF-16 au lieu de UTF-8)

**Solution Windows (PowerShell)** :

```powershell
# Convertir le fichier
Get-Content "data.sql" -Encoding Unicode | Set-Content "data_utf8.sql" -Encoding UTF8

# Puis exécuter
psql -U postgres -d tpf -f data_utf8.sql
```

### Erreur : "constraint violation", "duplicate key"

**Cause** : Données dupliquées ou séquences mal initialisées

**Solution** :

```bash
# Réinitialiser les séquences
psql -U postgres -d tpf << EOF
SELECT setval('site_id_site_seq', (SELECT MAX(id_site) FROM site)+1);
SELECT setval('patrouilleur_id_patrouilleur_seq', (SELECT MAX(id_patrouilleur) FROM patrouilleur)+1);
SELECT setval('userapp_id_userapp_seq', (SELECT MAX(id_userapp) FROM userapp)+1);
SELECT setval('message_id_message_seq', (SELECT MAX(id_message) FROM message)+1);
SELECT setval('alerte_id_alerte_seq', (SELECT MAX(id_alerte) FROM alerte)+1);
EOF
```

---

## Sauvegardes et restauration

### Créer une sauvegarde complète

```bash
# Dump avec données et structure
pg_dump -U postgres -d tpf -F c > tpf_backup_$(date +%Y%m%d_%H%M%S).dump

# Dump au format texte (plus lisible)
pg_dump -U postgres -d tpf > tpf_backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restaurer une sauvegarde

```bash
# Depuis un dump en format custom
pg_restore -U postgres -d tpf -Fc tpf_backup_20250115_143000.dump

# Depuis un dump en format texte
psql -U postgres -d tpf -f tpf_backup_20250115_143000.sql
```

### Mettre à jour `application.properties`

Après avoir créé/restauré la base, configurez la connexion dans l'application :

**Fichier** : `The-Peregrine-Fund-Project/Site/src/main/resources/application.properties`

```properties
# PostgreSQL Connection
spring.datasource.url=jdbc:postgresql://localhost:5432/tpf
spring.datasource.username=postgres
spring.datasource.password=VotreMotDePasse

# Si vous utilisez l'utilisateur dédié
spring.datasource.username=tpf_user
spring.datasource.password=VotreMotDePasse123
```

---

## Résumé

1. **Créer la base** : `CREATE DATABASE tpf ...`
2. **Charger schéma** : `psql -U postgres -d tpf -f structure_lisible.sql`
3. **Charger données test (optionnel)**: `psql -U postgres -d tpf -f data_lisible.sql`
4. **Vérifier** : `psql -U postgres -d tpf -c "SELECT COUNT(*) FROM site;"`
5. **Configurer app** : Mettre à jour `application.properties` avec les credentials
6. **Démarrer** : Application se connecte automatiquement
