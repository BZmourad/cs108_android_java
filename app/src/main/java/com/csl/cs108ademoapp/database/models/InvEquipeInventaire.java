package com.csl.cs108ademoapp.database.models;

public class InvEquipeInventaire extends Model {
    public String CodeEmplacement;
    public String codeEquipe;
    public String codemission;
    public String Codeinv;

    public InvEquipeInventaire() {}

    public InvEquipeInventaire(String codeEmplacement, String codeEquipe, String codemission, String codeinv) {
        this.CodeEmplacement = codeEmplacement;
        this.codeEquipe = codeEquipe;
        this.codemission = codemission;
        this.Codeinv = codeinv;
    }
}