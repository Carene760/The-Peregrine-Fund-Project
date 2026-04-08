package com.example.serveur.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "typealerte")
public class TypeAlerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_typealerte")
    private int idTypeAlerte;
    
    @Column(name = "zone", nullable = false)
    private String zone;
    
    // Getters et setters
    public int getIdTypeAlerte() { return idTypeAlerte; }
    public void setIdTypeAlerte(int idTypeAlerte) { this.idTypeAlerte = idTypeAlerte; }
    
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    @OneToMany(mappedBy = "typeAlerte")
    private List<FonctionZoneAlerte> fonctionZoneAlertes;
}