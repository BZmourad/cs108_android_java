package com.csl.cs108ademoapp.database.models;

import java.util.Date;

public class DetInvImmo extends Model {
    public String CodeInventaire;
    public String CodeImmo;
    public String CodeEmpTh;
    public Date DateLecture;
    public String UserLecture;
    public String Observation;
    public Date DateValidation;
    public String Uservalidation;
    public Integer EtatCorrection;
    public String codeEmpInv;
    public String codeEmpCor;
    public String codemission;
    public String codeEquipe;
    public String codeArticle;
    public String codeEtat_Corr;
    public String empl_Corr;
    public String codeBarre_Corr;
    public String NumSerie_Corr;
    public Integer nbr_Valid;
    public Integer nbr_Crt;
    public Integer nbr_Crt_valid;
    public String Descp_immo;
    public Integer issync;
    public String RFIDCode_Corr;

    public DetInvImmo() {}

    public DetInvImmo(String codeInventaire, String codeImmo) {
        this.CodeInventaire = codeInventaire;
        this.CodeImmo = codeImmo;
    }
}