package com.example.theperegrinefund;

public class Site {
    private int idSite;
    private String nom;
    private String region;
    private double surface;
    private String decree;
    public Site( String nom, String region, double surface, String decree)
    {
        this.nom = nom;
        this.region = region;
        this.surface = surface;
        this.decree = decree;
    }
    public String getNom()
    {
        return this.nom;
    }
    public String getRegion()
    {
        return this.region;
    }
    public double getSurface()
    {
        return this.surface;
    }
    public String getDecree()
    {
        return this.decree;
    }
    public void setNom( String s)
    {
        this.nom = s;
    }
    public void setRegion( String s)
    {
        this.region = s;
    }
    public void setSurface( double s)
    {
        this.surface = s;
    }
    public void setDecree( String s)
    {
        this.decree = s;
    }
}
