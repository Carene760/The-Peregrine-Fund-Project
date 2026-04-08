package com.example.serveur.model;

import jakarta.persistence.*;

@Entity
@Table(name = "status_message")
public class StatusMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_message")
    private int idStatusMessage;

    @Column(name = "status")
    private String status;

    // --- Getters & Setters ---
    public int getIdStatusMessage() {
        return idStatusMessage;
    }

    public void setIdStatusMessage(int idStatusMessage) {
        this.idStatusMessage = idStatusMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
