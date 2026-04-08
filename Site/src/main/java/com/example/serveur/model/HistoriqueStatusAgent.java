package com.example.serveur.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_status_agent")
public class HistoriqueStatusAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private int idHistorique;

    @Column(name = "date_changement")
    private LocalDateTime dateChangement;

    @ManyToOne
    @JoinColumn(name = "id_status_agent", nullable = false)
    private StatusAgent statusAgent;

    @ManyToOne
    @JoinColumn(name = "id_patrouilleur", nullable = false)
    private Patrouilleurs patrouilleur;

    // Getters and setters
    public int getIdHistorique() {
        return idHistorique;
    }

    public void setIdHistorique(int idHistorique) {
        this.idHistorique = idHistorique;
    }

    public LocalDateTime getDateChangement() {
        return dateChangement;
    }

    public void setDateChangement(LocalDateTime dateChangement) {
        this.dateChangement = dateChangement;
    }

    public StatusAgent getStatusAgent() {
        return statusAgent;
    }

    public void setStatusAgent(StatusAgent statusAgent) {
        this.statusAgent = statusAgent;
    }

    public Patrouilleurs getPatrouilleur() {
        return patrouilleur;
    }

    public void setPatrouilleur(Patrouilleurs patrouilleur) {
        this.patrouilleur = patrouilleur;
    }
}
