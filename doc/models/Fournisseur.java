package com.csl.cs108ademoapp.database.models;

import java.util.Date;

public class Fournisseur extends Model {
    public String IDFournisseur;
    public String CompteCollectif;
    public String CompteCharge;
    public String NumTVA;
    public String Reference;
    public String Notes;
    public Integer Etat;
    public Date DateHeureCreation;
    public String AuteurCreation;
    public Date DateHeureModification;
    public String AuteurModification;
    public String Vide;
    public String Lettrage;
    public String Code;
    public String Adresse_email;
    public String Code_Postal;
    public String Fax;
    public Double Numero_Fournisseur;
    public String Pays;
    public String ADR1;
    public String ADR2;
    public String CP;
    public String VILLE;
    public String Conttact;

    public Fournisseur() {}

    public Fournisseur(String idFournisseur, String code) {
        this.IDFournisseur = idFournisseur;
        this.Code = code;
    }
}