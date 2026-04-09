package com.example.serveur.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private int idMessage;
    
    @Column(name = "date_commencement", nullable = false)
    private LocalDateTime dateCommencement;
    
    @Column(name = "date_signalement", nullable = false)
    private LocalDateTime dateSignalement;
    
    @Column(name = "pointrepere")
    private String pointRepere;
    
    @Column(name = "surface_approximative")
    private Double surfaceApproximative;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "direction", nullable = false, length = 20)
    private String direction;
    
    @Column(name = "renfort")
    private Boolean renfort;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @ManyToOne
    @JoinColumn(name = "id_intervention", nullable = false)
    private Intervention intervention;
    
    @ManyToOne
    @JoinColumn(name = "iduserapp", nullable = false)
    private UserApp userApp;
    
    // Getters et setters
    public int getIdMessage() { return idMessage; }
    public void setIdMessage(int idMessage) { this.idMessage = idMessage; }
    
    public LocalDateTime getDateCommencement() { return dateCommencement; }
    public void setDateCommencement(LocalDateTime dateCommencement) { this.dateCommencement = dateCommencement; }
    
    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }
    
    public String getPointRepere() { return pointRepere; }
    public void setPointRepere(String pointRepere) { this.pointRepere = pointRepere; }
    
    public Double getSurfaceApproximative() { return surfaceApproximative; }
    public void setSurfaceApproximative(Double surfaceApproximative) { this.surfaceApproximative = surfaceApproximative; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    
    public Boolean getRenfort() { return renfort; }
    public void setRenfort(Boolean renfort) { this.renfort = renfort; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Intervention getIntervention() { return intervention; }
    public void setIntervention(Intervention intervention) { this.intervention = intervention; }
    
    public UserApp getUserApp() { return userApp; }
    public void setUserApp(UserApp userApp) { this.userApp = userApp; }
}