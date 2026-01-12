package com.csl.cs108ademoapp.database.models;

public class Attributs extends Model {
    public String Description;
    public Boolean obligatoire;
    public String type;
    public String valeurDefaut;
    public String libCode;
    public Boolean isAffiche;
    public Boolean isInconnu;

    public Attributs() {}

    public Attributs(String description, String type, String libCode) {
        this.Description = description;
        this.type = type;
        this.libCode = libCode;
    }
}