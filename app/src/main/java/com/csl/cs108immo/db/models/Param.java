package com.csl.cs108immo.db.models;

public class Param {
    private int id;
    private String adress;
    private String mode;
    private String projet;

    // Constructeurs
    public Param() {}

    public Param(String adress, String mode, String projet) {
        this.adress = adress;
        this.mode = mode;
        this.projet = projet;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getProjet() { return projet; }
    public void setProjet(String projet) { this.projet = projet; }
}
