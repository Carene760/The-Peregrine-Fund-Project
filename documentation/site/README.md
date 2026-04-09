# Documentation du module Site

Ce dossier centralise la documentation du serveur web du projet.

## Structure de version

```text
documentation/site/
├── README.md
└── v1/
    ├── README.md
    ├── EXECUTION_v1.md
    ├── ARCHITECTURE_v1.md
    ├── FONCTIONNALITE_v1.md
    └── CHANGELOG.md
```

## Regle de versioning

- Le dossier `v1` represente un snapshot stable de reference.
- Une evolution majeure cree un nouveau dossier `v2`.
- Les corrections de texte mineures peuvent etre appliquees dans la meme version.
- Le suivi des changements se fait dans `v1/CHANGELOG.md`.

## Ordre de lecture recommande

1. `v1/README.md`
2. `v1/ARCHITECTURE_v1.md`
3. `v1/FONCTIONNALITE_v1.md`
4. `v1/EXECUTION_v1.md`
5. `v1/CHANGELOG.md`

## Public cible

- Developpeurs backend
- Mainteneurs applicatifs
- DevOps / administrateurs

## Perimetre

Cette documentation couvre:

- architecture globale de l application Site,
- configuration et execution par environnement,
- regles metier principales des services,
- maintenance et pannes frequentes.

Derniere mise a jour: 2026-04-09
