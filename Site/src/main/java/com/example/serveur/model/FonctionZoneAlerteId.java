package com.example.serveur.model;


import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FonctionZoneAlerteId implements Serializable {

    private int idFonction;
    private int idTypeAlerte;

    // Constructeurs
    public FonctionZoneAlerteId() {}

    public FonctionZoneAlerteId(int idFonction, int idTypeAlerte) {
        this.idFonction = idFonction;
        this.idTypeAlerte = idTypeAlerte;
    }

    // Getters & Setters
    public int getIdFonction() {
        return idFonction;
    }

    public void setIdFonction(int idFonction) {
        this.idFonction = idFonction;
    }

    public int getIdTypeAlerte() { return idTypeAlerte;
    }

    public void setIdTypeAlerte(int idTypeAlerte) {
        this.idTypeAlerte = idTypeAlerte;
    }

    // equals & hashCode obligatoires
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FonctionZoneAlerteId)) return false;
        FonctionZoneAlerteId that = (FonctionZoneAlerteId) o;
        return Objects.equals(idFonction, that.idFonction) &&
               Objects.equals(idTypeAlerte, that.idTypeAlerte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFonction, idTypeAlerte);
    }
}
