package com.example.serveur.model;
import jakarta.persistence.*;


@Entity
@Table(name = "fonction_zonealerte")
public class FonctionZoneAlerte {

    @EmbeddedId
    private FonctionZoneAlerteId id;

    @ManyToOne
    @MapsId("idFonction")
    @JoinColumn(name = "id_fonction")
    private Fonction fonction;

    @ManyToOne
    @MapsId("idTypeAlerte")
    @JoinColumn(name = "id_typealerte")
    private TypeAlerte typeAlerte;

    // Constructeurs
    public FonctionZoneAlerte() {}

    public FonctionZoneAlerte(Fonction fonction, TypeAlerte typeAlerte) {
        this.fonction = fonction;
        this.typeAlerte = typeAlerte;
        this.id = new FonctionZoneAlerteId(fonction.getIdFonction(), typeAlerte.getIdTypeAlerte());
    }

    // Getters & Setters
    public FonctionZoneAlerteId getId() {
        return id;
    }

    public void setId(FonctionZoneAlerteId id) {
        this.id = id;
    }

    public Fonction getFonction() {
        return fonction;
    }

    public void setFonction(Fonction fonction) {
        this.fonction = fonction;
    }

    public TypeAlerte getTypeAlerte() {
        return typeAlerte;
    }

    public void setTypeAlerte(TypeAlerte typeAlerte) {
        this.typeAlerte = typeAlerte;
    }
}