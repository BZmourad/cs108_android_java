package com.csl.cs108ademoapp.database.models;

public class Utilisateur extends Model {
    public String CodeUtil;
    public String NomPrenomUtil;
    public String MotPasse;
    public String login;

    public Utilisateur() {}

    public Utilisateur(String codeUtil, String nomPrenomUtil, String login) {
        this.CodeUtil = codeUtil;
        this.NomPrenomUtil = nomPrenomUtil;
        this.login = login;
    }
}