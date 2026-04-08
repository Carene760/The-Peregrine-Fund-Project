
package com.example.serveur.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Intervention")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_Intervention;

    private String intervention;

    // Getter et Setter pour id_Intervention
    public Integer getId_Intervention() {
        return id_Intervention;
    }

    public void setId_Intervention(Integer id_Intervention) {
        this.id_Intervention = id_Intervention;
    }

    // Getter et Setter pour intervention
    public String getIntervention() {
        return intervention;
    }

    public void setIntervention(String intervention) {
        this.intervention = intervention;
    }
}
