package com.example.theperegrinefund;

public class HistoriqueMessageStatus {
    private int idHistorique;
    private String dateChangement;
    private int idStatusMessage;
    private int idMessage;
    private StatusMessage status;
    private Message message;

    // Constructeur par défaut
    public HistoriqueMessageStatus() {
    }
    public HistoriqueMessageStatus(String changement, int idStatusMessage, int idMessage) {
     
        this.dateChangement = changement;
        this.idStatusMessage = idStatusMessage;
        this.idMessage = idMessage;
    }
    // Constructeur avec 4 paramètres
    public HistoriqueMessageStatus(int id, String changement, int idStatusMessage, int idMessage) {
        this.idHistorique = id;
        this.dateChangement = changement;
        this.idStatusMessage = idStatusMessage;
        this.idMessage = idMessage;
    }

    // Getters et setters...
    public int getIdHistorique() {
        return idHistorique;
    }

    public void setIdHistorique(int idHistorique) {
        this.idHistorique = idHistorique;
    }

    public String getDateChangement() {
        return dateChangement;
    }

    public void setDateChangement(String dateChangement) {
        this.dateChangement = dateChangement;
    }

    public int getIdStatusMessage() {
        return idStatusMessage;
    }

    public void setIdStatusMessage(int idStatusMessage) {
        this.idStatusMessage = idStatusMessage;
    }

    public int getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(int idMessage) {
        this.idMessage = idMessage;
    }

    public StatusMessage getStatus() {
        return status;
    }

    public void setStatus(StatusMessage status) {
        this.status = status;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}