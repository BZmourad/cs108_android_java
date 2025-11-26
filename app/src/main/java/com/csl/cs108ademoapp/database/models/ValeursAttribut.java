package com.csl.cs108ademoapp.database.models;

public class ValeursAttribut extends Model {
    public String codeAttribut;
    public String Valeur;
    public Boolean isdefault;

    public ValeursAttribut() {}

    public ValeursAttribut(String codeAttribut, String valeur) {
        this.codeAttribut = codeAttribut;
        this.Valeur = valeur;
    }
}