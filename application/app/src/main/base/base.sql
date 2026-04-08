CREATE TABLE Fonction(
   Id_Fonction SERIAL PRIMARY KEY,
   fonction VARCHAR(50)
);

CREATE TABLE Site(
   Id_Site SERIAL PRIMARY KEY,
   Nom VARCHAR(50) NOT NULL,
   Region VARCHAR(50) NOT NULL,
   Surface DECIMAL(15,2),
   Decree VARCHAR(50),
   latitude DECIMAL(9,6),
   longitude DECIMAL(9,6)
);

CREATE TABLE TypeAlerte(
   Id_TypeAlerte SERIAL PRIMARY KEY,
   Zone VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE status_message(
   Id_status_message SERIAL PRIMARY KEY,
   status VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE status_agent(
   Id_status_agent SERIAL PRIMARY KEY,
   status VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Intervention(
   Id_Intervention SERIAL PRIMARY KEY,
   intervention VARCHAR(50)
);

CREATE TABLE Patrouilleurs(
   Id_Patrouilleur SERIAL PRIMARY KEY,
   Nom VARCHAR(100) NOT NULL,
   role VARCHAR(50),
   telephone VARCHAR(20) NOT NULL,
   Date_recrutement DATE,
   Id_Site INT NOT NULL REFERENCES Site(Id_Site)
);

CREATE TABLE UserApp(
   IdUserApp SERIAL PRIMARY KEY,
   motDePasse VARCHAR(100) NOT NULL,
   login VARCHAR(50) NOT NULL,
   Id_Patrouilleur INT NOT NULL UNIQUE REFERENCES Patrouilleurs(Id_Patrouilleur)
);

CREATE TABLE User_(
   Id_User SERIAL PRIMARY KEY,
   Nom VARCHAR(50) NOT NULL,
   email VARCHAR(50) NOT NULL UNIQUE,
   Telephone VARCHAR(50) NOT NULL,
   Adresse VARCHAR(50) NOT NULL,
   mot_de_passe VARCHAR(155) NOT NULL,
   Id_Fonction INT NOT NULL REFERENCES Fonction(Id_Fonction)
);

CREATE TABLE Message(
   Id_Message SERIAL PRIMARY KEY,
   Date_Commencement TIMESTAMP NOT NULL,
   Date_signalement TIMESTAMP NOT NULL UNIQUE,
   PointRepere TEXT,
   Surface_Approximative DECIMAL(15,2),
   Description TEXT,
   Direction VARCHAR(20) NOT NULL,
   renfort BOOLEAN,
   longitude DECIMAL(15,8),
   latitude DECIMAL(15,8),
   Id_Intervention INT NOT NULL REFERENCES Intervention(Id_Intervention),
   IdUserApp INT NOT NULL REFERENCES UserApp(IdUserApp)
);

CREATE TABLE Alerte(
   Id_Alerte SERIAL PRIMARY KEY,
   Id_Site INT NOT NULL REFERENCES Site(Id_Site),
   Id_Message INT NOT NULL REFERENCES Message(Id_Message),
   Id_TypeAlerte INT NOT NULL REFERENCES TypeAlerte(Id_TypeAlerte)
);

CREATE TABLE historique_message_status(
   Id_historique SERIAL PRIMARY KEY,
   date_changement TIMESTAMP,
   Id_status_message INT NOT NULL REFERENCES status_message(Id_status_message),
   Id_Message INT NOT NULL REFERENCES Message(Id_Message)
);

CREATE TABLE historique_status_agent(
   Id_historique SERIAL PRIMARY KEY,
   date_changement TIMESTAMP,
   Id_status_agent INT NOT NULL REFERENCES status_agent(Id_status_agent),
   Id_Patrouilleur INT NOT NULL REFERENCES Patrouilleurs(Id_Patrouilleur)
);

CREATE TABLE message_patrouilleur(
   Id_message_patrouilleur SERIAL PRIMARY KEY,
   Id_Patrouilleur INT NOT NULL REFERENCES Patrouilleurs(Id_Patrouilleur),
   Id_Message INT NOT NULL REFERENCES Message(Id_Message),
   UNIQUE(Id_Patrouilleur)
);

-- SITE
INSERT INTO site (nom, region, surface, decree, latitude, longitude) VALUES
('Tsimembo-Manambolomaty', 'Menabe', 50000.00, 'Décret 2002-123', -19.8000, 44.5000),
('Mandrozo', 'Menabe', 45000.00, 'Décret 2002-124', -19.7000, 44.6000),
('Bemanevika', 'Sofia', 35000.00, 'Décret 2002-125', -14.5000, 49.8000),
('Mahimborondro', 'Sofia', 30000.00, 'Décret 2002-126', -14.6000, 49.9000);


-- TYPE ALERTE
INSERT INTO typealerte (zone) VALUES
('Rouge'),
('Orange'),
('Jaune'),
('Vert');

-- STATUS MESSAGE
INSERT INTO status_message (status) VALUES
('Debut de feu'),
('En cours'),
('Maitrise');

-- STATUS AGENT
INSERT INTO status_agent (status) VALUES
('Disponible'),
('En mission'),
('Indisponible');

-- INTERVENTION
INSERT INTO intervention (intervention) VALUES
('Possible'),
('Partielle'),
('Impossible');


-- PATROUILLEURS
INSERT INTO patrouilleurs (nom, role, telephone, date_recrutement, id_site) VALUES
('Rasolofoniaina Jean', 'Chef patrouille', '0349322431', '2020-01-15', 1),
('Rakotoarisoa Marie', 'Patrouilleur', '0383817421', '2021-03-20', 2),
('Andrianasolo Hery', 'Patrouilleur', '0382305086', '2019-11-10', 3),
('Andrianasolo Hery', 'Patrouilleur', '0382305086', '2019-11-10', 4);

-- USERAPP
INSERT INTO userapp (motdepasse, login, id_patrouilleur) VALUES
('pass123', 'jean_r', 1),
('pass456', 'marie_r', 2),
('pass789', 'hery_a', 3);

-- USER_
INSERT INTO user_ (nom, email, telephone, adresse, mot_de_passe, id_fonction) VALUES
('Rakotomalala Lala', 'lala.rak@gmail.com', '0321112233', 'Antananarivo', 'mdp1', 1),
('Ranaivo Hanta', 'hanta.rana@gmail.com', '0322223344', 'Fianarantsoa', 'mdp2', 2);

