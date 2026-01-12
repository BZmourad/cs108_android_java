package com.csl.cs108ademoapp.database.models;

public class Proprietaire extends Model {
    public String CodeProprietaire;
    public String Libelle;
    public String Addresse;

    public Proprietaire() {}

    public Proprietaire(String codeProprietaire, String libelle) {
        this.CodeProprietaire = codeProprietaire;
        this.Libelle = libelle;
    }
}