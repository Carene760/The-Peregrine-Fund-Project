package com.example.theperegrinefund;

public class TypeAlerte {
    private int idTypeAlerte;
    private String zone;

    public TypeAlerte(int idTypeAlerte, String zone) {
        this.idTypeAlerte = idTypeAlerte;
        this.zone = zone;
    }
    public int getIdTypeAlerte() {
        return idTypeAlerte;
    }
    public String getZone() {
        return zone;
    }
    public void setIdTypeAlerte(int idTypeAlerte) {
        this.idTypeAlerte = idTypeAlerte;
    }
    public void setZone(String zone) {
        this.zone = zone;
    }
}
