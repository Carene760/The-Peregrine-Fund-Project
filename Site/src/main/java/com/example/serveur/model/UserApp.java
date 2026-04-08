package com.example.serveur.model;

import jakarta.persistence.*;

@Entity
@Table(name = "userapp")
public class UserApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduserapp")
    private int idUserApp;
    
    @Column(name = "motdepasse")
    private String motDePasse;
    
    private String login;
    
    @OneToOne
    @JoinColumn(name = "id_patrouilleur")
    private Patrouilleurs patrouilleur;
    
    // Getters et Setters
    public int getIdUserApp() { return idUserApp; }
    public void setIdUserApp(int idUserApp) { this.idUserApp = idUserApp; }
    
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public Patrouilleurs getPatrouilleur() { return patrouilleur; }
    public void setPatrouilleur(Patrouilleurs patrouilleur) { this.patrouilleur = patrouilleur; }
}