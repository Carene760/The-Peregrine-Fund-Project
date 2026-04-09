-- Donnees de base pour la V2
-- Prerequis: schema execute via bdd/v2/structure_v2.sql

BEGIN;

INSERT INTO evenement (nom, date, description)
VALUES
    ('Incendie secteur nord', CURRENT_DATE, 'Evenement de test V2'),
    ('Incendie secteur est', CURRENT_DATE, 'Evenement de test V2');

-- Exemple d association si des messages existent deja.
-- Adaptez la clause WHERE selon votre environnement de test.
UPDATE message
SET id_evenement = (SELECT id_evenement FROM evenement ORDER BY id_evenement LIMIT 1)
WHERE id_evenement IS NULL
  AND id_message IN (SELECT id_message FROM message ORDER BY id_message LIMIT 5);

COMMIT;
