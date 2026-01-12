package com.csl.cs108ademoapp.database.models;

import java.util.Date;

public class InvImmo extends Model {
    public String CodeInventaire;
    public Date DateCreation;
    public Integer EtatInventaire;
    public Date DateCloture;
    public String UserCloture;
    public String UserCreation;
    public String Description;
    public Boolean verif_art_inv;
    public Boolean affich_art_inv;
    public Integer nbrAtt;
    public Boolean multi_eq_mis;

    public InvImmo() {}

    public InvImmo(String codeInventaire, String description) {
        this.CodeInventaire = codeInventaire;
        this.Description = description;
    }
}