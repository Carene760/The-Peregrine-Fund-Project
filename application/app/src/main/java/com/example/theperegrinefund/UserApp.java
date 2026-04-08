package com.example.theperegrinefund;

public class UserApp {
    private int idUserApp;
    private String login;
    private String mot_de_passe;
    private Patrouilleur agent;
    private String phoneNumber;
    private String content;


    public UserApp(String phoneNumber, String content) {
        this.phoneNumber = phoneNumber;
        this.content = content;
    }
    public int getIdUserApp() {
        return idUserApp;
    }

    public String getLogin() {
        return login;
    }

    public String getMot_de_passe() {
        return mot_de_passe;
    }

    public Patrouilleur getAgent() {
        return agent;
    }

    public void setIdUserApp(int idUserApp) {
        this.idUserApp = idUserApp;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setMot_de_passe(String mot_de_passe) {
        this.mot_de_passe = mot_de_passe;
    }

    public void setAgent(Patrouilleur agent) {
        this.agent = agent;
    }
     public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getContent() {
        return content;
    }
    public boolean isValid() {
        return phoneNumber != null && !phoneNumber.isEmpty()
                && content != null && !content.isEmpty();
    }
}
