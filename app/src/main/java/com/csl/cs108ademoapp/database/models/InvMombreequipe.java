package com.csl.cs108ademoapp.database.models;

public class InvMombreequipe extends Model {
    public String CodeUtil;
    public String codeEquipe;
    public String Codeinv;
    public String NOM;

    public InvMombreequipe() {}

    public InvMombreequipe(String codeUtil, String codeEquipe, String codeinv) {
        this.CodeUtil = codeUtil;
        this.codeEquipe = codeEquipe;
        this.Codeinv = codeinv;
    }
}