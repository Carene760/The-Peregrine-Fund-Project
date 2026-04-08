package com.example.serveur.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Patrouilleurs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_patrouilleur")
    private int idPatrouilleur;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "role", length = 50)
    private String role;

    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    @Column(name = "date_recrutement")
    private LocalDateTime dateRecrutement;

    @ManyToOne
    @JoinColumn(name = "id_site", nullable = false)
    private Site site;

    // --- Getters ---
    public int getIdPatrouilleur() { return idPatrouilleur; }
    public String getNom() { return nom; }
    public String getRole() { return role; }
    public String getTelephone() { return telephone; }
    public LocalDateTime getDateRecrutement() { return dateRecrutement; }
    public Site getSite() { return site; }

    // --- Setters ---
    public void setIdPatrouilleur(int idPatrouilleur) { this.idPatrouilleur = idPatrouilleur; }
    public void setNom(String nom) { this.nom = nom; }
    public void setRole(String role) { this.role = role; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setDateRecrutement(LocalDateTime dateRecrutement) { this.dateRecrutement = dateRecrutement; }
    public void setSite(Site site) { this.site = site; }
}
