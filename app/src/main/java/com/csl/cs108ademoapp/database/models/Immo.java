package com.csl.cs108ademoapp.database.models;

import java.util.Date;

public class Immo extends Model {
    public String Descp_immo;
    public String code_etat;
    public String fournsr;
    public String Code_immo;
    public String Employee;
    public String type_etat;
    public String Num_serie;
    public String entite_juridique;
    public Integer prix_acquis;
    public String emplacement;
    public Date date_desactivation;
    public Date date_aquisition;
    public Integer prix_cession;
    public Integer val_cession;
    public Date dat_service;
    public Integer Chek;
    public String code_barre;
    public String code_famille;
    public String attribut_set;
    public String code_article;
    public String motif;
    public String user;
    public Date date_cre;
    public Double id_immo;
    public String attValr;
    public Integer issync;
    public String Code_RFID;

    public Immo() {}

    public Immo(String code_immo, String descp_immo) {
        this.Code_immo = code_immo;
        this.Descp_immo = descp_immo;
    }
}