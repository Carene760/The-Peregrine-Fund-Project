package com.example.theperegrinefund;

public class StatusAgent {
    private int idStatusAgent;
    private String status;

    public StatusAgent(int idStatusAgent, String status) {
        this.idStatusAgent = idStatusAgent;
        this.status = status;
    }

    public int getIdStatusAgent() {
        return idStatusAgent;
    }

    public String getStatus() {
        return status;
    }

    public void setIdStatusAgent(int idStatusAgent) {
        this.idStatusAgent = idStatusAgent;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
