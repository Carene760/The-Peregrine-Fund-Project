CREATE TABLE evenement (
    id_evenement SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    description TEXT
);

ALTER TABLE message ADD COLUMN id_evenement INT,
    ADD FOREIGN KEY (id_evenement) REFERENCES evenement(id_evenement);

ALTER TABLE message ADD COLUMN date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

