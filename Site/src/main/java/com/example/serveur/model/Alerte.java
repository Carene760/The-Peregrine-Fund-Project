package com.example.serveur.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alerte")
public class Alerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerte")
    private int idAlerte;
    
    @ManyToOne
    @JoinColumn(name = "id_site", nullable = false)
    private Site site;
    
    @ManyToOne
    @JoinColumn(name = "id_message", nullable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "id_typealerte", nullable = false)
    private TypeAlerte typeAlerte;

    // Constructeurs
    public Alerte() {}
    
    public Alerte(Site site, Message message, TypeAlerte typeAlerte) {
        this.site = site;
        this.message = message;
        this.typeAlerte = typeAlerte;
    }    
    
    // Getters et setters
    public int getIdAlerte() { return idAlerte; }
    public void setIdAlerte(int idAlerte) { this.idAlerte = idAlerte; }
    
    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }
    
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
    
    public TypeAlerte getTypeAlerte() { return typeAlerte; }
    public void setTypeAlerte(TypeAlerte typeAlerte) { this.typeAlerte = typeAlerte; }

}