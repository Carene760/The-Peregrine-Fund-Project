package com.example.serveur.model;

import jakarta.persistence.*;

@Entity
@Table(name = "User_")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private int idUser;

    @Column(name = "Nom", nullable = false, length = 50)
    private String nom;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "Telephone", nullable = false, length = 50)
    private String telephone;

    @Column(name = "Adresse", nullable = false, length = 50)
    private String adresse;

    @Column(name = "mot_de_passe", nullable = false, length = 155)
    private String motDePasse;

    @ManyToOne
    @JoinColumn(name = "id_fonction", nullable = false)
    private Fonction fonction;

    // Getters and setters
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Fonction getFonction() {
        return fonction;
    }

    public void setFonction(Fonction fonction) {
        this.fonction = fonction;
    }
}