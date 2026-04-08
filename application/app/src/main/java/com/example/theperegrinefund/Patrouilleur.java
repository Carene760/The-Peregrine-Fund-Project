package com.example.theperegrinefund;

public class Patrouilleur {
    private int idPatrouilleur;
    private String nom;
    private String role;
    private String telephone;
    private String dateRecrutement;  // car dans ta table c’est VARCHAR(50)
    private Site site;

    public Patrouilleur(String nom, String role, String telephone, String dateRecrutement, Site site) {
        this.nom = nom;
        this.role = role;
        this.telephone = telephone;
        this.dateRecrutement = dateRecrutement;
        this.site = site;
    }

    // --- Getters ---
    public int getIdPatrouilleur() {
        return idPatrouilleur;
    }
    public String getNom() {
        return nom;
    }
    public String getRole() {
        return role;
    }
    public String getTelephone() {
        return telephone;
    }
    public String getDateRecrutement() {
        return dateRecrutement;
    }
    public Site getSite() {
        return site;
    }

    // --- Setters ---
    public void setIdPatrouilleur(int idPatrouilleur) {
        this.idPatrouilleur = idPatrouilleur;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public void setDateRecrutement(String dateRecrutement) {
        this.dateRecrutement = dateRecrutement;
    }
    public void setSite(Site site) {
        this.site = site;
    }
}
