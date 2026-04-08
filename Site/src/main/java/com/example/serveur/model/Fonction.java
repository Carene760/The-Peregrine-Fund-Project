package com.example.serveur.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "fonction")
public class Fonction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Fonction")
    private int idFonction;

    @Column(name = "fonction", nullable = false)
    private String fonction;

    // Constructors
    public Fonction() {}

    public Fonction(String fonction) {
        this.fonction = fonction;
    }

    // Getters and Setters
    public int getIdFonction() {
        return idFonction;
    }

    public void setIdFonction(int idFonction) {
        this.idFonction = idFonction;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }
    @OneToMany(mappedBy = "fonction")
    private List<FonctionZoneAlerte> fonctionZoneAlertes;

    // + getter et setter
    public List<FonctionZoneAlerte> getFonctionZoneAlertes() {
        return fonctionZoneAlertes;
    }

    public void setFonctionZoneAlertes(List<FonctionZoneAlerte> fonctionZoneAlertes) {
        this.fonctionZoneAlertes = fonctionZoneAlertes;
    }
}
