# Documentation Base de Données - The Peregrine Fund

## 📋 Vue d'ensemble

Ce dossier contient la documentation technique de la base de données PostgreSQL pour le projet **The Peregrine Fund**. Elle est destinée aux développeurs et administrateurs système.

**Base de données** : `tpf`
**SGBD** : PostgreSQL 16.4+
**Serveur** : localhost:5432

---

## 📁 Structure de ce dossier

```
documentation/bdd/
├── README.md                    # Ce fichier (index et guide)
├── SCHEMA_v1.md                # Description complète du schéma et relations
├── EXECUTION_v1.md             # Procédures d'installation et d'exécution
├── CHANGELOG.md                # Historique des modifications
└── /sqlscripts/                # Scripts SQL référencés
    ├── structure.sql           (original du dump PostgreSQL)
    ├── structure_lisible.sql   (version lisible)
    ├── data.sql                (original du dump PostgreSQL)
    └── data_lisible.sql        (version lisible)
```

---

## 🚀 Démarrage rapide

### Pour les nouveaux développeurs :

1. **Lire d'abord** : [SCHEMA_v1.md](SCHEMA_v1.md) - Comprenez la structure
2. **Puis installer** : [EXECUTION_v1.md](EXECUTION_v1.md) - Étapes de mise en place
3. **Consulter** : [CHANGELOG.md](CHANGELOG.md) - Historique des évolutions

### Pour les administrateurs DB :

1. Suivez [EXECUTION_v1.md](EXECUTION_v1.md) pour la procédure complète
2. Consultez [SCHEMA_v1.md](SCHEMA_v1.md) pour comprendre les dépendances
3. Mettez à jour [CHANGELOG.md](CHANGELOG.md) après chaque modification

---

## 📊 Informations clés

| Propriété | Valeur |
|-----------|--------|
| **Nom de la base** | `tpf` |
| **Nombre de tables** | 20+ |
| **Tables principales** | `message`, `alerte`, `site`, `patrouilleur` |
| **Type d'authentification** | Utilisateur PostgreSQL (défini dans app.properties) |
| **Encodage** | UTF-8 |
| **Localisation** | Système décimal (points pour décimales) |

---

## 🔄 Flux de versioning

Ce dossier utilise un **versioning hybride** :
- Les fichiers sont nommés avec leur version (`SCHEMA_v1.md`, `EXECUTION_v1.md`)
- Chaque nouvelle version du fichier incrémente le numéro (`v1` → `v2`)
- Les anciennes versions sont conservées pour traçabilité
- [CHANGELOG.md](CHANGELOG.md) centralise l'historique complet

**Exemple de progression** :
```
2025-01-15: Création initiale (v1)
2025-01-20: Ajout table audit → v2
2025-02-01: Migration schéma → v3
```

---

## 📝 Comment contribuer

Quand vous modifiez la base de données :

1. **Mettez à jour la documentation** dans l'ordre :
   - [SCHEMA_v1.md](SCHEMA_v1.md) ou créez `SCHEMA_v2.md`
   - [EXECUTION_v1.md](EXECUTION_v1.md) si procédures changent
   - [CHANGELOG.md](CHANGELOG.md) - Ajoutez une entrée

2. **Créez une nouvelle version** si modification majeure :
   ```
   cp SCHEMA_v1.md SCHEMA_v2.md
   # Editez SCHEMA_v2.md
   # Mettez à jour CHANGELOG.md
   ```

3. **Pour les migrations**, créez un script de migration dans `/sqlscripts/migrations/`

---

## ❓ FAQ

**Q: Par où commencer si je suis nouveau ?**
A: Lisez [SCHEMA_v1.md](SCHEMA_v1.md) en entier, puis [EXECUTION_v1.md](EXECUTION_v1.md).

**Q: Comment re-créer la base localement ?**
A: Suivez la section "Installation complète" dans [EXECUTION_v1.md](EXECUTION_v1.md).

**Q: Quand upgrader la version d'un fichier ?**
A: - Changements majeurs (restructuration) → nouvelle version (v1 → v2)
   - Corrections mineures/clarifications → mettez à jour l'existant
   - Consultez [CHANGELOG.md](CHANGELOG.md) pour les critères

**Q: Où trouver l'ordre d'exécution des .sql ?**
A: [EXECUTION_v1.md](EXECUTION_v1.md) section "Procédure d'exécution par étapes"

---

## 🔗 Ressources complémentaires

- **Documentation technique principale** : `../DOCUMENTATION_TECHNIQUE.md`
- **Configuration application** : Voir `application.properties` (chemin : `The-Peregrine-Fund-Project/Site/src/main/resources/`)
- **Code source (data access)** : Dossier `src/main/java/com/example/serveur/repository/`

---

**Dernière mise à jour** : 2025-01-15
**Auteur** : Assistant de Documentation
**Statut** : Version initiale (v1)
