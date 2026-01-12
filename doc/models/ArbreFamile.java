package com.csl.cs108ademoapp.database.models;

public class ArbreFamile extends Model {
    public String code;
    public String Libelle;
    public String Idparent;

    public ArbreFamile() {}

    public ArbreFamile(String code, String libelle, String idparent) {
        this.code = code;
        this.Libelle = libelle;
        this.Idparent = idparent;
    }
}