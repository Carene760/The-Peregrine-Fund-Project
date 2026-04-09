# CHANGELOG - documentation/site/v2

## 2026-04-10

### Version 2.0

- Creation du dossier `v2` pour la documentation Site.
- Ajout de `README.md` v2 (positionnement et ecarts vs v1).
- Ajout de `ARCHITECTURE_v2.md`:
  - bascule detection message vers type explicite,
  - parser cle=valeur,
  - prise en compte de `date_envoi`,
  - integration service webhook auto.
- Ajout de `FONCTIONNALITE_v2.md`:
  - nouveaux contrats message ALERTE/LOGIN/STATUS,
  - validations obligatoires par type,
  - workflow create/delete webhook,
  - configuration v2 en properties.
- Ajout de `EXECUTION_v2.md`:
  - migration BDD (`date_envoi`),
  - configuration v2,
  - checklist de tests de bascule.

## Difference structurante vs v1

- v1: detection type basee sur nb de separateurs.
- v2: detection type explicite + parser cle=valeur.

## Regle de maintenance

- Toute evolution majeure v2 -> mise a jour de ce changelog.
- Si rupture de contrat, creer v3.
