# EXECUTION V2

## Prerequis

- PostgreSQL disponible
- Base cible du projet deja creee

## Etapes

1. Executer la structure V2

```sql
\i bdd/v2/structure_v2.sql
```

2. Charger les donnees V2

```sql
\i bdd/v2/data.sql
```

3. Verifier

```sql
SELECT * FROM evenement;
SELECT id_message, id_evenement FROM message ORDER BY id_message DESC LIMIT 20;
```

## Rollback simple (si environnement de test)

```sql
ALTER TABLE message DROP CONSTRAINT IF EXISTS message_id_evenement_fkey;
ALTER TABLE message DROP COLUMN IF EXISTS id_evenement;
DROP TABLE IF EXISTS evenement;
```
