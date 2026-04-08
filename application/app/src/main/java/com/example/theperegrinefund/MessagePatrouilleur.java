package com.example.theperegrinefund;

public class MessagePatrouilleur {
    private int idMessagePatrouilleur;
    private Patrouilleur patrouilleur;
    private Message message;

    public MessagePatrouilleur(Patrouilleur patrouilleur, Message message) {
        this.patrouilleur = patrouilleur;
        this.message = message;
    }

    // --- Getters ---
    public int getIdMessagePatrouilleur() {
        return idMessagePatrouilleur;
    }
    public Patrouilleur getPatrouilleur() {
        return patrouilleur;
    }
    public Message getMessage() {
        return message;
    }

    // --- Setters ---
    public void setIdMessagePatrouilleur(int idMessagePatrouilleur) {
        this.idMessagePatrouilleur = idMessagePatrouilleur;
    }
    public void setPatrouilleur(Patrouilleur patrouilleur) {
        this.patrouilleur = patrouilleur;
    }
    public void setMessage(Message message) {
        this.message = message;
    }
}

