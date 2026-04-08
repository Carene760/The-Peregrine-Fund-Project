# Documentation Base de Données - The Peregrine Fund

## Vue d'ensemble

Ce dossier contient la documentation technique de la base de données PostgreSQL pour le projet **The Peregrine Fund**. Elle est destinée aux développeurs et administrateurs système.

**Base de données** : `tpf`
**SGBD** : PostgreSQL 16.4+
**Serveur** : localhost:5432

---

## Structure de ce dossier

```
documentation/bdd/
├── README.md                    # Ce fichier (index et guide)
├── SCHEMA_v1.md                # Description complète du schéma et relations
├── EXECUTION_v1.md             # Procédures d'installation et d'exécution  
```

---

## Démarrage rapide

### Recommendé :

1. **Lire d'abord** : [SCHEMA_v1.md](SCHEMA_v1.md) - Comprenez la structure
2. **Puis consulter**: [EXECUTION_v1.md](EXECUTION_v1.md) - Étapes de mise en place

---

## Flux de versioning

Ce dossier utilise un **versioning hybride** :

- Les fichiers sont nommés avec leur version (`SCHEMA_v1.md`, `EXECUTION_v1.md`)
- Chaque nouvelle version du fichier incrémente le numéro (`v1` → `v2`)
- Les anciennes versions sont conservées pour traçabilité

**Exemple de progression** :

```
2025-01-15: Création initiale (v1)
2025-01-20: Ajout table audit → v2
2025-02-01: Migration schéma → v3
```

---

## Comment contribuer

Quand vous modifiez la base de données :

1. **Mettez à jour la documentation** dans l'ordre :

   - [SCHEMA_v1.md](SCHEMA_v1.md) ou créez `SCHEMA_v2.md`
   - [EXECUTION_v1.md](EXECUTION_v1.md) si procédures changent
2. **Créez une nouvelle version** si modification majeure :

   ```
   cp SCHEMA_v1.md SCHEMA_v2.md
   # Editez SCHEMA_v2.md
   ```
3. **Pour les migrations**, créez un script de migration dans `/bdd/migrations/`

---

## Ressources complémentaires

- **Documentation technique principale** : `../DOCUMENTATION_TECHNIQUE.md`
- **Configuration application** : Voir `application.properties` (chemin : `The-Peregrine-Fund-Project/Site/src/main/resources/`)
- **Code source (data access)** : Dossier `src/main/java/com/example/serveur/repository/`
