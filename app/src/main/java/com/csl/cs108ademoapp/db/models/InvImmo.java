package com.csl.cs108ademoapp.db.models;

import java.util.Date;

public class InvImmo {
    private int id;
    private String codeInventaire;
    private Date dateCreation;
    private int etatInventaire;
    private Date dateCloture;
    private String userCloture;
    private String userCreation;
    private String description;
    private boolean verifArtInv;
    private boolean affichArtInv;
    private int nbrAtt;
    private boolean multiEqMis;

    // Constructeurs
    public InvImmo() {}

    public InvImmo(String codeInventaire, String userCreation, String description) {
        this.codeInventaire = codeInventaire;
        this.userCreation = userCreation;
        this.description = description;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodeInventaire() { return codeInventaire; }
    public void setCodeInventaire(String codeInventaire) { this.codeInventaire = codeInventaire; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public int getEtatInventaire() { return etatInventaire; }
    public void setEtatInventaire(int etatInventaire) { this.etatInventaire = etatInventaire; }

    public Date getDateCloture() { return dateCloture; }
    public void setDateCloture(Date dateCloture) { this.dateCloture = dateCloture; }

    public String getUserCloture() { return userCloture; }
    public void setUserCloture(String userCloture) { this.userCloture = userCloture; }

    public String getUserCreation() { return userCreation; }
    public void setUserCreation(String userCreation) { this.userCreation = userCreation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isVerifArtInv() { return verifArtInv; }
    public void setVerifArtInv(boolean verifArtInv) { this.verifArtInv = verifArtInv; }

    public boolean isAffichArtInv() { return affichArtInv; }
    public void setAffichArtInv(boolean affichArtInv) { this.affichArtInv = affichArtInv; }

    public int getNbrAtt() { return nbrAtt; }
    public void setNbrAtt(int nbrAtt) { this.nbrAtt = nbrAtt; }

    public boolean isMultiEqMis() { return multiEqMis; }
    public void setMultiEqMis(boolean multiEqMis) { this.multiEqMis = multiEqMis; }
}