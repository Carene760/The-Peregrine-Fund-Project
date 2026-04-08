CREATE TABLE Fonction(
   Id_Fonction INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   fonction VARCHAR(50)
);

CREATE TABLE Site(
   Id_Site INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   Nom VARCHAR(50) NOT NULL,
   Region VARCHAR(50) NOT NULL,
   Surface DECIMAL(15,2),
   Decree VARCHAR(50)
);

CREATE TABLE TypeAlerte(
   Id_TypeAlerte INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   Zone VARCHAR(50) NOT NULL,
   PRIMARY KEY(Id_TypeAlerte),
   UNIQUE(Zone)
);

CREATE TABLE status_message(
   Id_status_message INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   status VARCHAR(50) NOT NULL,
   PRIMARY KEY(Id_status_message),
   UNIQUE(status)
);

CREATE TABLE status_agent(
   Id_status_agent INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   status VARCHAR(50) NOT NULL,
   PRIMARY KEY(Id_status_agent),
   UNIQUE(status)
);

CREATE TABLE Patrouilleurs(
   Id_Patrouilleur INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   Nom VARCHAR(100) NOT NULL,
   role VARCHAR(50),
   telephone VARCHAR(20) NOT NULL,
   Date_recrutement VARCHAR(50),
   Id_Site INT NOT NULL,
   FOREIGN KEY(Id_Site) REFERENCES Site(Id_Site)
);

CREATE TABLE UserApp(
   Id_UserApp  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   motDePasse VARCHAR(100) NOT NULL,
   login VARCHAR(50) NOT NULL,
   Id_Patrouilleur INT NOT NULL,
   UNIQUE(Id_Patrouilleur),
   FOREIGN KEY(Id_Patrouilleur) REFERENCES Patrouilleurs(Id_Patrouilleur)
);

CREATE TABLE User_(
   Id_User INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   Nom VARCHAR(50) NOT NULL,
   email VARCHAR(50) NOT NULL,
   Telephone VARCHAR(50) NOT NULL,
   Adresse VARCHAR(50) NOT NULL,
   mot_de_passe VARCHAR(155) NOT NULL,
   Id_Fonction INT NOT NULL,
   UNIQUE(email),
   FOREIGN KEY(Id_Fonction) REFERENCES Fonction(Id_Fonction)
);

CREATE TABLE Message(
   Id_Message INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   Date_Commencement TIMESTAMP NOT NULL,
   Date_signalement TIMESTAMP NOT NULL UNIQUE,
   contenu_code TEXT,
   PointRepere TEXT,
   Surface_Approximative DECIMAL(15,2),
   Description TEXT,
   Direction VARCHAR(20) NOT NULL,
   Elements_visibles VARCHAR(100),
   Degats VARCHAR(100),
   Id_UserApp INT NOT NULL,
   FOREIGN KEY(Id_UserApp) REFERENCES UserApp(Id_UserApp)
);

CREATE TABLE Alerte(
   Id_Alerte INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   Id_Site INT NOT NULL,
   Id_Message INT NOT NULL,
   Id_TypeAlerte INT NOT NULL,
   FOREIGN KEY(Id_Site) REFERENCES Site(Id_Site),
   FOREIGN KEY(Id_Message) REFERENCES Message(Id_Message),
   FOREIGN KEY(Id_TypeAlerte) REFERENCES TypeAlerte(Id_TypeAlerte)
);

CREATE TABLE historique_message_status(
   Id_historique INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   date_changement DATETIME,
   Id_status_message INT NOT NULL,
   Id_Message INT NOT NULL,
   FOREIGN KEY(Id_status_message) REFERENCES status_message(Id_status_message),
   FOREIGN KEY(Id_Message) REFERENCES Message(Id_Message)
);

CREATE TABLE historique_status_agent(
   Id_historique INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   date_changement DATETIME,
   Id_status_agent INT NOT NULL,
   Id_Patrouilleur INT NOT NULL,
   FOREIGN KEY(Id_status_agent) REFERENCES status_agent(Id_status_agent),
   FOREIGN KEY(Id_Patrouilleur) REFERENCES Patrouilleurs(Id_Patrouilleur)
);

CREATE TABLE message_patrouilleur(
   Id_message_patrouilleur INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   Id_Patrouilleur INT NOT NULL,
   Id_Message INT NOT NULL,
   UNIQUE(Id_Patrouilleur),
   FOREIGN KEY(Id_Patrouilleur) REFERENCES Patrouilleurs(Id_Patrouilleur),
   FOREIGN KEY(Id_Message) REFERENCES Message(Id_Message)
);
