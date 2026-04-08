-- Necessaire pour la fonction UNACCENT utilisee dans un trigger
CREATE EXTENSION IF NOT EXISTS unaccent;

-- =============================================================
-- TABLES
-- =============================================================

CREATE TABLE fonction (
    id_fonction SERIAL PRIMARY KEY,
    fonction VARCHAR(255)
);

CREATE TABLE intervention (
    id_intervention SERIAL PRIMARY KEY,
    intervention VARCHAR(255)
);

CREATE TABLE site (
    id_site SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL,
    surface DOUBLE PRECISION,
    decree VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

CREATE TABLE typealerte (
    id_typealerte SERIAL PRIMARY KEY,
    zone VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE status_agent (
    id_status_agent SERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE status_message (
    id_status_message SERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE patrouilleurs (
    id_patrouilleur SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    role VARCHAR(50),
    telephone VARCHAR(20) NOT NULL,
    date_recrutement TIMESTAMP(6),
    id_site INTEGER NOT NULL,
    email VARCHAR(255),
    CONSTRAINT uniq_nom_par_site UNIQUE (id_site, nom),
    CONSTRAINT patrouilleurs_id_site_fkey
        FOREIGN KEY (id_site) REFERENCES site(id_site)
);

CREATE TABLE user_ (
    id_user SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    telephone VARCHAR(50) NOT NULL,
    adresse VARCHAR(50) NOT NULL,
    mot_de_passe VARCHAR(155),
    id_fonction INTEGER NOT NULL,
    CONSTRAINT user__id_fonction_fkey
        FOREIGN KEY (id_fonction) REFERENCES fonction(id_fonction)
);

CREATE TABLE userapp (
    iduserapp SERIAL PRIMARY KEY,
    motdepasse VARCHAR(255) NOT NULL,
    login VARCHAR(255) NOT NULL,
    id_patrouilleur INTEGER NOT NULL UNIQUE,
    CONSTRAINT userapp_id_patrouilleur_fkey
        FOREIGN KEY (id_patrouilleur) REFERENCES patrouilleurs(id_patrouilleur)
);

CREATE TABLE message (
    id_message SERIAL PRIMARY KEY,
    date_commencement TIMESTAMP NOT NULL,
    date_signalement TIMESTAMP NOT NULL,
    pointrepere VARCHAR(255),
    surface_approximative DOUBLE PRECISION,
    description VARCHAR(255),
    direction VARCHAR(20) NOT NULL,
    renfort BOOLEAN,
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    id_intervention INTEGER NOT NULL,
    iduserapp INTEGER NOT NULL,
    CONSTRAINT ck_dates CHECK (date_signalement > date_commencement),
    CONSTRAINT message_id_intervention_fkey
        FOREIGN KEY (id_intervention) REFERENCES intervention(id_intervention),
    CONSTRAINT message_iduserapp_fkey
        FOREIGN KEY (iduserapp) REFERENCES userapp(iduserapp)
);

CREATE TABLE alerte (
    id_alerte SERIAL PRIMARY KEY,
    id_site INTEGER NOT NULL,
    id_message INTEGER NOT NULL,
    id_typealerte INTEGER NOT NULL,
    CONSTRAINT alerte_id_site_fkey
        FOREIGN KEY (id_site) REFERENCES site(id_site),
    CONSTRAINT alerte_id_message_fkey
        FOREIGN KEY (id_message) REFERENCES message(id_message),
    CONSTRAINT alerte_id_typealerte_fkey
        FOREIGN KEY (id_typealerte) REFERENCES typealerte(id_typealerte)
);

CREATE TABLE fonction_zonealerte (
    id_fonction INTEGER NOT NULL,
    id_typealerte INTEGER NOT NULL,
    PRIMARY KEY (id_fonction, id_typealerte),
    CONSTRAINT fk6x3sssh3lgp5yq8aj1n7121x9
        FOREIGN KEY (id_fonction) REFERENCES fonction(id_fonction),
    CONSTRAINT fkvkhjq398yybr705es83met8i
        FOREIGN KEY (id_typealerte) REFERENCES typealerte(id_typealerte)
);

CREATE TABLE historique_status_agent (
    id_historique SERIAL PRIMARY KEY,
    date_changement TIMESTAMP,
    id_status_agent INTEGER NOT NULL,
    id_patrouilleur INTEGER NOT NULL,
    CONSTRAINT historique_status_agent_id_status_agent_fkey
        FOREIGN KEY (id_status_agent) REFERENCES status_agent(id_status_agent),
    CONSTRAINT historique_status_agent_id_patrouilleur_fkey
        FOREIGN KEY (id_patrouilleur) REFERENCES patrouilleurs(id_patrouilleur)
);

CREATE TABLE historique_message_status (
    id_historique SERIAL PRIMARY KEY,
    date_changement TIMESTAMP,
    id_status INTEGER,
    id_message INTEGER,
    CONSTRAINT fk_status
        FOREIGN KEY (id_status) REFERENCES status_message(id_status_message),
    CONSTRAINT fk_message
        FOREIGN KEY (id_message) REFERENCES message(id_message)
);

CREATE TABLE message_patrouilleur (
    id_message_patrouilleur SERIAL PRIMARY KEY,
    id_patrouilleur INTEGER NOT NULL UNIQUE,
    id_message INTEGER NOT NULL,
    CONSTRAINT message_patrouilleur_id_patrouilleur_fkey
        FOREIGN KEY (id_patrouilleur) REFERENCES patrouilleurs(id_patrouilleur),
    CONSTRAINT message_patrouilleur_id_message_fkey
        FOREIGN KEY (id_message) REFERENCES message(id_message)
);

-- =============================================================
-- FONCTIONS + TRIGGERS
-- =============================================================

CREATE OR REPLACE FUNCTION check_telephone_sites()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  -- Verifie si le meme telephone existe deja sur un autre site
  IF EXISTS (
       SELECT 1
       FROM patrouilleurs
       WHERE telephone = NEW.telephone
         AND id_site <> NEW.id_site
     ) THEN
     RAISE EXCEPTION
       'Le telephone % est deja utilise sur un autre site', NEW.telephone;
  END IF;

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION sync_responsable_to_user()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    fonction_id INT;
    email_pattern VARCHAR;
BEGIN
    IF NEW.role = 'Responsable' THEN
        SELECT id_fonction
        INTO fonction_id
        FROM fonction
        WHERE fonction = 'Responsable';

        IF NEW.email IS NOT NULL AND NEW.email <> '' THEN
            email_pattern := NEW.email;
        ELSE
            email_pattern := LOWER(REPLACE(UNACCENT(NEW.nom), ' ', '.')) || '@patrouille.mg';
        END IF;

        INSERT INTO user_ (nom, email, telephone, adresse, mot_de_passe, id_fonction)
        VALUES (
            NEW.nom,
            email_pattern,
            NEW.telephone,
            'Antananarivo',
            'responsable123',
            fonction_id
        )
        ON CONFLICT (email) DO NOTHING;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trig_check_tel
BEFORE INSERT OR UPDATE ON patrouilleurs
FOR EACH ROW
EXECUTE FUNCTION check_telephone_sites();

CREATE TRIGGER trig_sync_responsable
AFTER INSERT ON patrouilleurs
FOR EACH ROW
EXECUTE FUNCTION sync_responsable_to_user();
