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

CREATE TABLE status_message(
   Id_status_message SERIAL PRIMARY KEY,
   status VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE historique_message_status(
   Id_historique SERIAL PRIMARY KEY,
   date_changement TIMESTAMP,
   Id_status_message INT NOT NULL REFERENCES status_message(Id_status_message),
   Id_Message INT NOT NULL REFERENCES Message(Id_Message)
);

CREATE TABLE Intervention(
   Id_Intervention SERIAL PRIMARY KEY,
   intervention VARCHAR(50)
);