package com.csl.cs108ademoapp.database.models;

public class InvAttributInvImmo extends Model {
    public String name;
    public String codeatribut;
    public Boolean saisie;
    public String Codeinv;
    public Boolean afficher;
    public Boolean validation;

    public InvAttributInvImmo() {}

    public InvAttributInvImmo(String name, String codeinv) {
        this.name = name;
        this.Codeinv = codeinv;
    }
}