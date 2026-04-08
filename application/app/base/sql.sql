CREATE TABLE Fonction(
   Id_Fonction COUNTER,
   fonction VARCHAR(50),
   PRIMARY KEY(Id_Fonction)
);

CREATE TABLE Site(
   Id_Site COUNTER,
   Nom VARCHAR(50) NOT NULL,
   Region VARCHAR(50) NOT NULL,
   Surface DECIMAL(15,2),
   Decree VARCHAR(50),
   PRIMARY KEY(Id_Site)
);

CREATE TABLE TypeAlerte(
   Id_TypeAlerte COUNTER,
   Zone VARCHAR(50) NOT NULL,
   PRIMARY KEY(Id_TypeAlerte),
   UNIQUE(Zone)
);

CREATE TABLE status_message(
   Id_status_message COUNTER,
   status VARCHAR(50) NOT NULL,
   PRIMARY KEY(Id_status_message),
   UNIQUE(status)
);

CREATE TABLE status_agent(
   Id_status_agent COUNTER,
   status VARCHAR(50) NOT NULL,
   PRIMARY KEY(Id_status_agent),
   UNIQUE(status)
);

CREATE TABLE Intervention(
   Id_Intervention COUNTER,
   intervention VARCHAR(50),
   PRIMARY KEY(Id_Intervention)
);

CREATE TABLE Patrouilleurs(
   Id_Patrouilleur COUNTER,
   Nom VARCHAR(100) NOT NULL,
   role VARCHAR(50),
   telephone VARCHAR(20) NOT NULL,
   Date_recrutement VARCHAR(50),
   Id_Site INT NOT NULL,
   PRIMARY KEY(Id_Patrouilleur),
   FOREIGN KEY(Id_Site) REFERENCES Site(Id_Site)
);

CREATE TABLE UserApp(
   Id_UserApp COUNTER,
   motDePasse VARCHAR(100) NOT NULL,
   login VARCHAR(50) NOT NULL,
   Id_Patrouilleur INT NOT NULL,
   PRIMARY KEY(Id_UserApp),
   UNIQUE(Id_Patrouilleur),
   FOREIGN KEY(Id_Patrouilleur) REFERENCES Patrouilleurs(Id_Patrouilleur)
);

CREATE TABLE User_(
   Id_User COUNTER,
   Nom VARCHAR(50) NOT NULL,
   email VARCHAR(50) NOT NULL,
   Telephone VARCHAR(50) NOT NULL,
   Adresse VARCHAR(50) NOT NULL,
   mot_de_passe VARCHAR(155) NOT NULL,
   Id_Fonction INT NOT NULL,
   PRIMARY KEY(Id_User),
   UNIQUE(email),
   FOREIGN KEY(Id_Fonction) REFERENCES Fonction(Id_Fonction)
);

CREATE TABLE Message(
   Id_Message COUNTER,
   Date_Commencement DATETIME NOT NULL,
   Date_signalement DATETIME NOT NULL,
   contenu_code TEXT,
   PointRepere TEXT,
   Surface_Approximative DECIMAL(15,2),
   Description TEXT,
   Direction VARCHAR(20) NOT NULL,
   Elements_visibles VARCHAR(100),
   Degats VARCHAR(100),
   intervention INT NOT NULL,
   renfort LOGICAL,
   longitude DECIMAL(15,2),
   latitude DECIMAL(15,2),
   Id_Intervention INT NOT NULL,
   Id_UserApp INT NOT NULL,
   PRIMARY KEY(Id_Message),
   UNIQUE(Date_signalement),
   FOREIGN KEY(Id_Intervention) REFERENCES Intervention(Id_Intervention),
   FOREIGN KEY(Id_UserApp) REFERENCES UserApp(Id_UserApp)
);

CREATE TABLE Alerte(
   Id_Alerte COUNTER,
   Id_Site INT NOT NULL,
   Id_Message INT NOT NULL,
   Id_TypeAlerte INT NOT NULL,
   PRIMARY KEY(Id_Alerte),
   FOREIGN KEY(Id_Site) REFERENCES Site(Id_Site),
   FOREIGN KEY(Id_Message) REFERENCES Message(Id_Message),
   FOREIGN KEY(Id_TypeAlerte) REFERENCES TypeAlerte(Id_TypeAlerte)
);

CREATE TABLE historique_message_status(
   Id_historique COUNTER,
   date_changement DATETIME,
   Id_status_message INT NOT NULL,
   Id_Message INT NOT NULL,
   PRIMARY KEY(Id_historique),
   FOREIGN KEY(Id_status_message) REFERENCES status_message(Id_status_message),
   FOREIGN KEY(Id_Message) REFERENCES Message(Id_Message)
);

CREATE TABLE historique_status_agent(
   Id_historique COUNTER,
   date_changement DATETIME,
   Id_status_agent INT NOT NULL,
   Id_Patrouilleur INT NOT NULL,
   PRIMARY KEY(Id_historique),
   FOREIGN KEY(Id_status_agent) REFERENCES status_agent(Id_status_agent),
   FOREIGN KEY(Id_Patrouilleur) REFERENCES Patrouilleurs(Id_Patrouilleur)
);

CREATE TABLE message_patrouilleur(
   Id_message_patrouilleur COUNTER,
   Id_Patrouilleur INT NOT NULL,
   Id_Message INT NOT NULL,
   PRIMARY KEY(Id_message_patrouilleur),
   UNIQUE(Id_Patrouilleur),
   FOREIGN KEY(Id_Patrouilleur) REFERENCES Patrouilleurs(Id_Patrouilleur),
   FOREIGN KEY(Id_Message) REFERENCES Message(Id_Message)
);

-- Fonction
INSERT INTO Fonction(fonction) VALUES ('Administrateur');
INSERT INTO Fonction(fonction) VALUES ('Analyste');

-- Site
INSERT INTO Site(Nom, Region, Surface, Decree) VALUES ('Parc Andasibe', 'Alaotra-Mangoro', 1234.50, 'Decree-2020');
INSERT INTO Site(Nom, Region, Surface, Decree) VALUES ('Parc Ranomafana', 'Fianarantsoa', 987.20, 'Decree-2019');

-- TypeAlerte
INSERT INTO TypeAlerte(Zone) VALUES ('Zone Nord');
INSERT INTO TypeAlerte(Zone) VALUES ('Zone Sud');

-- status_message
INSERT INTO status_message(status) VALUES ('En attente');
INSERT INTO status_message(status) VALUES ('Traité');

-- status_agent
INSERT INTO status_agent(status) VALUES ('Disponible');
INSERT INTO status_agent(status) VALUES ('Occupé');

-- Intervention
INSERT INTO Intervention(intervention) VALUES ('Feu de brousse');
INSERT INTO Intervention(intervention) VALUES ('Coupe illégale');

-- Patrouilleurs
INSERT INTO Patrouilleurs(Nom, role, telephone, Date_recrutement, Id_Site) 
VALUES ('Rakoto Jean', 'Chef', '0341234567', '2023-01-15', 1);
INSERT INTO Patrouilleurs(Nom, role, telephone, Date_recrutement, Id_Site) 
VALUES ('Rasoa Marie', 'Adjoint', '0349876543', '2023-03-10', 2);

-- UserApp
INSERT INTO UserApp(motDePasse, login, Id_Patrouilleur) VALUES ('pwd123', 'rakoto', 1);
INSERT INTO UserApp(motDePasse, login, Id_Patrouilleur) VALUES ('pwd456', 'rasoa', 2);

-- User_
INSERT INTO User_(Nom, email, Telephone, Adresse, mot_de_passe, Id_Fonction) 
VALUES ('Ando', 'ando@example.com', '0331111111', 'Antananarivo', 'mdpando', 1);
INSERT INTO User_(Nom, email, Telephone, Adresse, mot_de_passe, Id_Fonction) 
VALUES ('Fara', 'fara@example.com', '0332222222', 'Toamasina', 'mdpfara', 2);

-- Message
INSERT INTO Message(Date_Commencement, Date_signalement, contenu_code, PointRepere, Surface_Approximative, 
                    Description, Direction, Elements_visibles, Degats, intervention, renfort, longitude, latitude, 
                    Id_Intervention, Id_UserApp)
VALUES ('2025-09-01 10:00:00', '2025-09-01 10:30:00', 'CODE001', 'Rivière Est', 200.50,
        'Feu détecté', 'Nord', 'Fumée visible', 'Arbres brûlés', 1, TRUE, 47.50, -18.90, 1, 1);

INSERT INTO Message(Date_Commencement, Date_signalement, contenu_code, PointRepere, Surface_Approximative, 
                    Description, Direction, Elements_visibles, Degats, intervention, renfort, longitude, latitude, 
                    Id_Intervention, Id_UserApp)
VALUES ('2025-09-01 14:00:00', '2025-09-01 14:20:00', 'CODE002', 'Colline Ouest', 150.00,
        'Coupes illégales repérées', 'Ouest', 'Bûches au sol', 'Forêt dégradée', 2, FALSE, 46.90, -19.10, 2, 2);

-- Alerte
INSERT INTO Alerte(Id_Site, Id_Message, Id_TypeAlerte) VALUES (1, 1, 1);
INSERT INTO Alerte(Id_Site, Id_Message, Id_TypeAlerte) VALUES (2, 2, 2);

-- historique_message_status
INSERT INTO historique_message_status(date_changement, Id_status_message, Id_Message) 
VALUES ('2025-09-01 11:00:00', 1, 1);
INSERT INTO historique_message_status(date_changement, Id_status_message, Id_Message) 
VALUES ('2025-09-01 15:00:00', 2, 2);

-- historique_status_agent
INSERT INTO historique_status_agent(date_changement, Id_status_agent, Id_Patrouilleur) 
VALUES ('2025-09-01 09:00:00', 1, 1);
INSERT INTO historique_status_agent(date_changement, Id_status_agent, Id_Patrouilleur) 
VALUES ('2025-09-01 13:30:00', 2, 2);

-- message_patrouilleur
INSERT INTO message_patrouilleur(Id_Patrouilleur, Id_Message) VALUES (1, 1);
INSERT INTO message_patrouilleur(Id_Patrouilleur, Id_Message) VALUES (2, 2);

