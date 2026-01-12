package com.csl.cs108ademoapp.database.models;

import java.util.Date;

public class InvEquipe extends Model {
    public String codeEquipe;
    public String NoEquipe;
    public String chefEquipe;
    public String Codeinv;
    public String UserCreation;
    public Date DateCreation;

    public InvEquipe() {}

    public InvEquipe(String codeEquipe, String codeinv) {
        this.codeEquipe = codeEquipe;
        this.Codeinv = codeinv;
    }
}