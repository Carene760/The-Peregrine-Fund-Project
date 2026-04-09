# SCHEMA V2

## Changements principaux

1. Nouvelle table `evenement`

- `id_evenement` SERIAL PRIMARY KEY
- `nom` VARCHAR(255) NOT NULL
- `date` DATE NOT NULL
- `description` TEXT

2. Extension table `message`

- nouvelle colonne `id_evenement` (nullable)
- cle etrangere vers `evenement(id_evenement)`
- nouvelle colonne `date_envoi` (TIMESTAMP, default CURRENT_TIMESTAMP)

## Impact metier

- Un message peut etre rattache a un evenement.
- Un evenement peut regrouper plusieurs messages.
- La liaison reste nullable pour conserver la retrocompatibilite.
- La date d'envoi est persistante et peut etre fournie par le format V2; sinon elle est valorisee automatiquement.
