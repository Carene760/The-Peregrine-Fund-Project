package com.example.serveur.model;

import jakarta.persistence.*;

@Entity
@Table(name = "status_agent")
public class StatusAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_agent")
    private int idStatusAgent;

    @Column(name = "status", length = 50, nullable = false, unique = true)
    private String status;

    // Getters and setters
    public int getIdStatusAgent() {
        return idStatusAgent;
    }

    public void setIdStatusAgent(int idStatusAgent) {
        this.idStatusAgent = idStatusAgent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
