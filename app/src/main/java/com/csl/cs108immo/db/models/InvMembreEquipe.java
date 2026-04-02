package com.csl.cs108immo.db.models;

public class InvMembreEquipe {
    private int id;
    private String codeUtil;
    private String codeEquipe;
    private String codeInv;
    private String nom;

    // Constructeurs
    public InvMembreEquipe() {}

    public InvMembreEquipe(String codeUtil, String codeEquipe, String codeInv, String nom) {
        this.codeUtil = codeUtil;
        this.codeEquipe = codeEquipe;
        this.codeInv = codeInv;
        this.nom = nom;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodeUtil() { return codeUtil; }
    public void setCodeUtil(String codeUtil) { this.codeUtil = codeUtil; }

    public String getCodeEquipe() { return codeEquipe; }
    public void setCodeEquipe(String codeEquipe) { this.codeEquipe = codeEquipe; }

    public String getCodeInv() { return codeInv; }
    public void setCodeInv(String codeInv) { this.codeInv = codeInv; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
