package com.example.serveur.model;

import jakarta.persistence.*;
@Entity
@Table(name = "message_patrouilleur")
public class MessagePatrouilleur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message_patrouilleur")
    private int idMessagePatrouilleur;

    @ManyToOne
    @JoinColumn(name = "id_patrouilleur", nullable = false, unique = true)
    private Patrouilleurs patrouilleur;

    @ManyToOne
    @JoinColumn(name = "id_message", nullable = false)
    private Message message;

    // Getters and setters

    public int getIdMessagePatrouilleur() {
        return idMessagePatrouilleur;
    }

    public void setIdMessagePatrouilleur(int idMessagePatrouilleur) {
        this.idMessagePatrouilleur = idMessagePatrouilleur;
    }

    public Patrouilleurs getPatrouilleur() {
        return patrouilleur;
    }

    public void setPatrouilleur(Patrouilleurs patrouilleur) {
        this.patrouilleur = patrouilleur;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
