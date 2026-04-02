package com.csl.cs108immo.db.models;

import java.util.Date;

public class InvMission {
    private int id;
    private String codeMission;
    private String etatMission;
    private Date dateCree;
    private String userCreation;
    private Date dateMaj;
    private String userMaj;
    private String description;
    private String codeInv;

    // Constructeurs
    public InvMission() {}

    public InvMission(String codeMission, String userCreation, String codeInv) {
        this.codeMission = codeMission;
        this.userCreation = userCreation;
        this.codeInv = codeInv;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodeMission() { return codeMission; }
    public void setCodeMission(String codeMission) { this.codeMission = codeMission; }

    public String getEtatMission() { return etatMission; }
    public void setEtatMission(String etatMission) { this.etatMission = etatMission; }

    public Date getDateCree() { return dateCree; }
    public void setDateCree(Date dateCree) { this.dateCree = dateCree; }

    public String getUserCreation() { return userCreation; }
    public void setUserCreation(String userCreation) { this.userCreation = userCreation; }

    public Date getDateMaj() { return dateMaj; }
    public void setDateMaj(Date dateMaj) { this.dateMaj = dateMaj; }

    public String getUserMaj() { return userMaj; }
    public void setUserMaj(String userMaj) { this.userMaj = userMaj; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCodeInv() { return codeInv; }
    public void setCodeInv(String codeInv) { this.codeInv = codeInv; }
}