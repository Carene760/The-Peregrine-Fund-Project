package com.example.serveur.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_message_status")
public class HistoriqueMessageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private int idHistorique;
    
    @Column(name = "date_changement")
    private LocalDateTime dateChangement;
    
    @ManyToOne
    @JoinColumn(name = "id_status", nullable = false)
    private StatusMessage status;
    
    @ManyToOne
    @JoinColumn(name = "id_message", nullable = false)
    private Message message;
    
    // Getters et setters
    public int getIdHistorique() { return idHistorique; }
    public void setIdHistorique(int idHistorique) { this.idHistorique = idHistorique; }
    
    public LocalDateTime getDateChangement() { return dateChangement; }
    public void setDateChangement(LocalDateTime dateChangement) { this.dateChangement = dateChangement; }
    
    public StatusMessage getStatus() { return status; }
    public void setIdStatus(StatusMessage status) { this.status = status; }
    
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
}