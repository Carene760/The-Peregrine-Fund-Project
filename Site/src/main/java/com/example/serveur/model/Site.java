package com.example.serveur.model;
import jakarta.persistence.*;

@Entity
@Table(name = "Site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_Site;

    @Column(nullable = false)
    private String Nom;

    @Column(nullable = false)
    private String Region;

    private Double Surface;
    private String Decree;

    private Double latitude;
    private Double longitude;

    // Getters et Setters
    public Integer getId_Site() { return id_Site; }
    public void setId_Site(Integer id_Site) { this.id_Site = id_Site; }

    public String getNom() { return Nom; }
    public void setNom(String Nom) { this.Nom = Nom; }

    public String getRegion() { return Region; }
    public void setRegion(String Region) { this.Region = Region; }

    public Double getSurface() { return Surface; }
    public void setSurface(Double Surface) { this.Surface = Surface; }

    public String getDecree() { return Decree; }
    public void setDecree(String Decree) { this.Decree = Decree; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
