package com.csl.cs108ademoapp.database.models;

import java.util.Date;

public class InvMission extends Model {
    public String codemission;
    public String etatmission;
    public Date DateCree;
    public String UserCreation;
    public Date DateMaj;
    public String usermaj;
    public String Description;
    public String Codeinv;

    public InvMission() {}

    public InvMission(String codemission, String codeinv) {
        this.codemission = codemission;
        this.Codeinv = codeinv;
    }
}