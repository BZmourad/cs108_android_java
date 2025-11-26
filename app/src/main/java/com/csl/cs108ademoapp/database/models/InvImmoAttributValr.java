package com.csl.cs108ademoapp.database.models;

public class InvImmoAttributValr extends Model {
    public String codeInv;
    public String codeImmo;
    public String codeAttrib;
    public String valr;
    public Integer issync;

    public InvImmoAttributValr() {}

    public InvImmoAttributValr(String codeInv, String codeImmo, String codeAttrib) {
        this.codeInv = codeInv;
        this.codeImmo = codeImmo;
        this.codeAttrib = codeAttrib;
    }
}