package com.example.theperegrinefund;
import com.google.gson.annotations.SerializedName;

public class Intervention {
    @SerializedName("id_Intervention")
    private int idIntervention;

    @SerializedName("intervention")
    private String intervention;

    // Ajout d'un constructeur par défaut
    public Intervention() {}

    public Intervention(int idIntervention, String intervention) {
        this.idIntervention = idIntervention;
        this.intervention = intervention;
    }

    public int getIdIntervention() {
        return idIntervention;
    }

    public void setIdIntervention(int idIntervention) {
        this.idIntervention = idIntervention;
    }

    public String getIntervention() {
        return intervention;
    }
    public void setIntervention(String intervention) {
        this.intervention = intervention;
    }   
}