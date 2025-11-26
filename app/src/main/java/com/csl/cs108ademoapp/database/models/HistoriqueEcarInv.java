package com.csl.cs108ademoapp.database.models;

import java.util.Date;

public class HistoriqueEcarInv extends Model {
    public String Code_immo;
    public String code_Inv;
    public Date DateValidation;
    public String Uservalidation;
    public String code_att_inv;
    public String code_att_th;
    public String typeVal;
    public String code_att;

    public HistoriqueEcarInv() {}

    public HistoriqueEcarInv(String code_immo, String code_Inv, String code_att) {
        this.Code_immo = code_immo;
        this.code_Inv = code_Inv;
        this.code_att = code_att;
    }
}