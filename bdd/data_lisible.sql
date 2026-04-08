-- =============================================================
-- Donnees lisibles de la base TPF
-- Version simplifiee a partir de bdd/data.sql
-- A executer apres bdd/structure_lisible.sql
-- =============================================================

SET search_path TO public;

BEGIN;

-- -------------------------------------------------------------
-- Tables de reference
-- -------------------------------------------------------------

INSERT INTO intervention (id_intervention, intervention) VALUES
    (1, 'Possible'),
    (2, 'Partielle'),
    (3, 'Impossible');

INSERT INTO site (id_site, nom, region, surface, decree, latitude, longitude) VALUES
    (1, 'Tsimembo-Manambolomaty', 'Menabe', 50000, 'Décret 2002-123', -19.8, 44.5),
    (2, 'Mandrozo', 'Menabe', 45000, 'Décret 2002-124', -19.7, 44.6),
    (3, 'Bemanevika', 'Sofia', 35000, 'Décret 2002-125', -14.5, 49.8),
    (4, 'Mahimborondro', 'Sofia', 30000, 'Décret 2002-126', -14.6, 49.9);

INSERT INTO typealerte (id_typealerte, zone) VALUES
    (1, 'Rouge'),
    (2, 'Orange'),
    (3, 'Jaune'),
    (4, 'Vert');

INSERT INTO status_message (id_status_message, status) VALUES
    (1, 'Debut de feu'),
    (2, 'En cours'),
    (3, 'Maitrise');

INSERT INTO status_agent (id_status_agent, status) VALUES
    (1, 'Disponible'),
    (2, 'En mission'),
    (3, 'Indisponible');

INSERT INTO fonction (id_fonction, fonction) VALUES
    (1, 'Directeur National'),
    (2, 'Administrateur'),
    (3, 'Subordonnée'),
    (4, 'Responsable');

INSERT INTO fonction_zonealerte (id_fonction, id_typealerte) VALUES
    (1, 1),
    (2, 1),
    (2, 2),
    (2, 3),
    (2, 4),
    (3, 1),
    (3, 2),
    (4, 1),
    (4, 2),
    (4, 3);

-- -------------------------------------------------------------
-- Utilisateurs et patrouilleurs
-- -------------------------------------------------------------

INSERT INTO patrouilleurs (id_patrouilleur, nom, role, telephone, date_recrutement, id_site, email) VALUES
    (5, 'Rasolofoniaina Jean', 'Responsable', '+261349322431', '2020-01-15 00:00:00', 1, 'harimalalaerickarandria@gmail.com'),
    (6, 'Rakotoarisoa Marie', 'Agent', '+261383817421', '2021-03-20 00:00:00', 2, NULL),
    (7, 'Andrianasolo Hery', 'Agent', '+261382305086', '2019-11-10 00:00:00', 3, NULL),
    (8, 'Andrianasolo Hery', 'Agent', '+261384984929', '2019-11-10 00:00:00', 4, NULL),
    (10, 'RAZAFIMANANTSOA Tsiresy', 'Agent', '+261346367580', '2020-01-15 00:00:00', 3, 'mntsiresy@gmail.com');

INSERT INTO user_ (id_user, nom, email, telephone, adresse, mot_de_passe, id_fonction) VALUES
    (1, 'Romeo Esther', 'romeesther66@gmail.com', '+261349833949', 'Antananarivo', 'password123', 2),
    (2, 'Mahaliana Razafimanjato', 'mahalianarazafimanjato@gmail.com', '+261382305086', 'Antsirabe', 'password123', 3),
    (3, 'Bakomalala Fenitra', 'bakomalalafenitra@gmail.com', '+261383817421', 'Fianarantsoa', 'password123', 1),
    (4, 'Harimalala Ericka', 'harimalalaerickarandria@gmail.com', '+261349322431', 'Toamasina', 'password123', 3),
    (6, 'RAZAFIMANANTSOA Tsiresy', 'mntsiresy@gmail.com', '+261346367580', 'Antananarivo', 'responsable123', 4);

INSERT INTO userapp (iduserapp, motdepasse, login, id_patrouilleur) VALUES
    (4, 'pass123', 'jean_r', 5),
    (5, 'pass456', 'marie_r', 6),
    (6, 'pass789', 'hery_a', 7),
    (7, 'pass012', 'agent', 8),
    (8, 'password4+2613463675804', 'agent10', 10);

-- -------------------------------------------------------------
-- Messages et alertes
-- -------------------------------------------------------------

INSERT INTO message (
    id_message, date_commencement, date_signalement, pointrepere,
    surface_approximative, description, direction, renfort,
    longitude, latitude, id_intervention, iduserapp
) VALUES
    (2,  '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointA', 50,   'Petit feu',    'Sud', FALSE, 47.50789,   -18.87919,   1, 6),
    (6,  '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointB', 150.5,'Feu moyen',    'Sud', FALSE, 47.508,     -18.88,      2, 6),
    (7,  '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointC', 300,  'Feu important','Sud', TRUE,  47.509,     -18.881,     2, 5),
    (10, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointD', 500,  'Feu critique', 'Sud', FALSE, 47.51,      -18.882,     3, 4),
    (11, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointA', 50,   'Petit feu',    'Sud', FALSE, 47.10789,   -18.87918,   1, 4),
    (12, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 47.511,     -18.883,     1, 6),
    (13, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 47.51188,   -18.8839,    1, 6),
    (14, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 47.51186,   -18.88397,   1, 6),
    (15, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 47.91186,   -18.98397,   1, 6),
    (16, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 46.91186,   -17.98397,   1, 7),
    (17, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 46.01186,   -17.08397,   1, 6),
    (18, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 46.01886,   -17.08399,   1, 6),
    (19, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 46.11886,   -17.18399,   1, 6),
    (20, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 46.11896,   -17.18389,   1, 6),
    (21, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 46.31896,   -17.28389,   1, 6),
    (22, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu faible',   'Sud', FALSE, 46.32896,   -17.21389,   1, 6),
    (23, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu important','Sud', TRUE,  47.32896,   -18.21389,   2, 5),
    (24, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu important','Sud', TRUE,  47.22896,   -18.23389,   2, 5),
    (25, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu important','Sud', TRUE,  47.24896,   -18.23387,   2, 5),
    (26, '2025-09-05 10:00:00', '2025-09-05 10:05:00', 'PointE', 900,  'Feu important','Sud', TRUE,  47.14896,   -18.27387,   2, 5),
    (36, '2025-09-11 15:29:00', '2025-09-11 16:29:51.31', NULL,  2184, NULL,           'Est', FALSE, -18.9190408, 47.5452566, 1, 7),
    (47, '2025-09-11 15:29:00', '2025-09-11 16:29:51.31', NULL,  2184, NULL,           'Est', FALSE, 47.5452566,  -18.9190408,1, 7),
    (51, '2025-12-01 03:27:00', '2025-12-23 12:27:50.002', NULL, 0,    NULL,           'Est', FALSE, 47.5325984,  -18.9859875,1, 5),
    (55, '2025-12-01 13:13:00', '2025-12-23 13:14:10.09',  NULL, 23,   NULL,           'Est', FALSE, 47.5326001,  -18.9859918,1, 5),
    (58, '2025-12-24 11:15:00', '2026-01-03 11:15:13.392', 'point', 21,'feu',         'Est', FALSE, 47.5453799,  -18.9190058,1, 5);

INSERT INTO alerte (id_alerte, id_site, id_message, id_typealerte) VALUES
    (1, 3, 2, 4),
    (2, 3, 6, 3),
    (3, 3, 7, 2),
    (5, 3, 10, 1),
    (6, 4, 11, 4),
    (7, 2, 12, 4),
    (8, 2, 13, 4),
    (9, 2, 14, 4),
    (10, 2, 15, 4),
    (11, 2, 16, 4),
    (12, 2, 17, 4),
    (13, 2, 18, 4),
    (14, 2, 19, 4),
    (15, 2, 20, 4),
    (16, 2, 21, 4),
    (17, 2, 22, 4),
    (18, 2, 23, 2),
    (19, 2, 24, 2),
    (20, 2, 25, 2),
    (21, 2, 26, 2),
    (22, 2, 36, 4),
    (23, 2, 47, 4),
    (24, 2, 51, 4),
    (25, 2, 55, 4),
    (26, 2, 58, 4);

INSERT INTO historique_message_status (id_historique, date_changement, id_status, id_message) VALUES
    (1,  '2025-09-05 19:23:02.537821', 1, 2),
    (2,  '2025-09-05 22:03:17.982591', 1, 6),
    (3,  '2025-09-05 22:14:51.283895', 2, 7),
    (6,  '2025-09-06 05:25:37.168109', 2, 10),
    (7,  '2025-09-06 23:32:36.072079', 1, 11),
    (8,  '2025-09-05 10:13:40', 3, 2),
    (9,  '2025-09-10 17:14:14.843616', 1, 12),
    (10, '2025-09-10 20:22:27.304738', 1, 13),
    (11, '2025-09-10 20:29:39.614159', 1, 14),
    (12, '2025-09-10 21:26:24.210809', 1, 15),
    (13, '2025-09-10 21:47:44.871689', 1, 16),
    (14, '2025-09-10 22:09:08.203571', 1, 17),
    (15, '2025-09-11 01:29:41.215755', 1, 18),
    (16, '2025-09-11 01:37:48.390992', 1, 19),
    (17, '2025-09-11 01:50:54.130791', 1, 20),
    (18, '2025-09-11 02:19:15.824485', 1, 21),
    (19, '2025-09-11 02:23:27.99219',  1, 22),
    (20, '2025-09-11 11:00:47.862247', 2, 23),
    (21, '2025-09-11 11:13:20.283051', 2, 24),
    (22, '2025-09-11 11:40:59.867536', 2, 25),
    (23, '2025-09-11 11:50:29.596045', 2, 26),
    (24, '2025-09-11 16:28:35.865916', 1, 36),
    (25, '2025-12-23 12:17:53.254573', 1, 47),
    (26, '2025-12-23 12:27:52.822643', 1, 51),
    (27, '2025-12-23 13:12:15',        3, 26),
    (29, '2025-12-23 13:14:15.730988', 1, 55),
    (30, '2025-12-23 13:15:07',        2, 55),
    (31, '2026-01-03 11:09:35',        2, 51),
    (32, '2026-01-03 11:15:15.365252', 1, 58);

-- Tables vides dans le dump source:
-- historique_status_agent
-- message_patrouilleur

COMMIT;

-- -------------------------------------------------------------
-- Ajustement des sequences pour rester coherent avec les IDs inseres
-- -------------------------------------------------------------

SELECT setval('public.alerte_id_alerte_seq', 26, true);
SELECT setval('public.fonction_id_fonction_seq', 4, true);
SELECT setval('public.historique_message_status_id_historique_seq', 32, true);
SELECT setval('public.historique_status_agent_id_historique_seq', 1, false);
SELECT setval('public.intervention_id_intervention_seq', 3, true);
SELECT setval('public.message_id_message_seq', 66, true);
SELECT setval('public.message_patrouilleur_id_message_patrouilleur_seq', 1, false);
SELECT setval('public.patrouilleurs_id_patrouilleur_seq', 10, true);
SELECT setval('public.site_id_site_seq', 4, true);
SELECT setval('public.status_agent_id_status_agent_seq', 3, true);
SELECT setval('public.status_message_id_status_message_seq', 3, true);
SELECT setval('public.typealerte_id_typealerte_seq', 4, true);
SELECT setval('public.user__id_user_seq', 12, true);
SELECT setval('public.userapp_iduserapp_seq', 8, true);
